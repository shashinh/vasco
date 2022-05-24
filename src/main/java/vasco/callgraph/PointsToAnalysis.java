/**
 * Copyright (C) 2013 Rohan Padhye
 * 
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package vasco.callgraph;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JNewArrayExpr;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import vasco.CallSite;
import vasco.Context;
import vasco.OldForwardInterProceduralAnalysis;
import vasco.ProgramRepresentation;
import vasco.soot.AbstractNullObj;
import vasco.soot.DefaultJimpleRepresentation;

/**
 * An inter-procedural analysis for constructing a context-sensitive call graph
 * on-the-fly.
 * 
 * <p>This analysis uses the value-based context-sensitive inter-procedural framework
 * and was developed as an example instantiation of the framework.</p>
 * 
 * <p>The analysis uses {@link PointsToGraph} objects as data flow values, which
 * in turn abstracts heap locations using allocation sites.<p>
 * 
 * <p><strong>Warning!</strong> The current implementation of this class uses
 * the old API (see {@link OldForwardInterProceduralAnalysis}) without separate
 * call/return flow functions. The developer is currently in the process of migrating
 * this implementation to the new API (see {@link vasco.ForwardInterProceduralAnalysis ForwardInterProceduralAnalysis}).</p>
 * 
 * @author Rohan Padhye
 */
public class PointsToAnalysis extends OldForwardInterProceduralAnalysis<SootMethod,Unit,PointsToGraph> {

	private static final SootMethod DUMMY_METHOD = new SootMethod("DUMMY_METHOD", Collections.EMPTY_LIST, Scene.v().getObjectType());
	
	/**
	 * A shared points-to graph that maintains information about objects
	 * reachable from static fields (modelled as fields of a dummy global variable).
	 * 
	 * <p>For static load/store statements, we union this points-to graph with the
	 * points-to graph in the flow function, perform the operation, and then
	 * separate stuff out again.</p>
	 */
	private PointsToGraph staticHeap;
	
	/**
	 * A set of classes whose static initialisers have been processed.
	 */
	private Set<SootClass> clinitCalled;
	

	public Map<AnyNewExpr, String> bciMap;
	public Map<AnyNewExpr, BciContainer> bciMap2;
	//public Map<AnyNewExpr, String> exprToMethodMap;
	

	/**
	 * Constructs a new points-to analysis as a forward-flow inter-procedural
	 * analysis.
	 */
	public PointsToAnalysis() {
		super();
		
		// Play around with these flags
		this.freeResultsOnTheFly = true;
		//this.verbose = true;
		
		// No classes statically initialised yet
		this.clinitCalled = new HashSet<SootClass>();
		
		// Create a static points-to graph with a single "global" root object
		this.staticHeap = topValue();
		this.staticHeap.assignNew(PointsToGraph.GLOBAL_LOCAL, PointsToGraph.GLOBAL_SITE);
		

		this.bciMap = new HashMap<AnyNewExpr, String>();
		this.bciMap2 = new HashMap<AnyNewExpr, BciContainer>();
		//this.exprToMethodMap = new HashMap<AnyNewExpr, String>();
	}

	/**
	 * Returns a points-to graph with the locals of main initialised to
	 * <tt>null</tt>, except the command-line arguments which are
	 * initialised to an array of strings.
	 */
	@Override
	public PointsToGraph boundaryValue(SootMethod entryPoint) {
		// For now we only support entry to the main method
		assert(entryPoint == Scene.v().getMainMethod());
		
		// Ok, start setting up entry value
		PointsToGraph entryValue = new PointsToGraph();		
	
		// Locals of main... (only reference types)
		SootMethod mainMethod = Scene.v().getMainMethod();
		for (Local local : mainMethod.getActiveBody().getLocals()) {
			if (local.getType() instanceof RefLikeType) {
				entryValue.assign(local, null);
			}
		}		
		
		// Command-line arguments to main...
		Local argsLocal = mainMethod.getActiveBody().getParameterLocal(0);
		NewArrayExpr argsExpr = new JNewArrayExpr(Scene.v().getRefType("java.lang.String"), IntConstant.v(0));
		entryValue.assignNew(argsLocal, argsExpr);
		entryValue.setFieldConstant(argsLocal, PointsToGraph.ARRAY_FIELD, PointsToGraph.STRING_CONST);
		
	
		return entryValue;
	}

	/**
	 * Returns a copy of the given points-to graph.
	 */
	@Override
	public PointsToGraph copy(PointsToGraph graph) {
		return new PointsToGraph(graph);
	}

	/**
	 * Performs operations on points-to graphs depending on the statement inside
	 * a CFG node. 
	 */
	@Override
	protected PointsToGraph flowFunction(Context<SootMethod,Unit,PointsToGraph> context, Unit unit, PointsToGraph in) 
	{

		// First set OUT to copy of IN (this is default for most statements).
		PointsToGraph out = new PointsToGraph(in);

		// This analysis is written assuming that units are statements (and not,
		// for example, basic blocks)
		assert (unit instanceof Stmt);
		Stmt stmt = (Stmt) unit;
		
		
		//SHASHIN
		
		if(isReflectedStatement(stmt)) {
			return out;
		} else if (stmt.toString().contains("<org.dacapo.harness.Callback: void init(org.dacapo.parser.Config)>")
				|| stmt.toString().contains("<org.dacapo.harness.Callback: void start(java.lang.String)>")
				|| stmt.toString().contains("<org.dacapo.harness.Callback: void stop(long)>")
				|| stmt.toString().contains("<org.dacapo.harness.Callback: void complete(java.lang.String,boolean)>")) { 
			return out;
		}
		
		
		
		int bci = -1;
		try {
				BytecodeOffsetTag bT = (BytecodeOffsetTag)unit.getTag("BytecodeOffsetTag");
				bci = bT.getBytecodeOffset();
		} catch (Exception ex) {
			//System.out.println("bci for unit is missing!");
		}
		
//		int byteCodeOffset = -1;
//		for(Tag tag : unit.getTags()) {
//			System.out.println("checking tag of type " + tag.getName());
//			if(tag instanceof BytecodeOffsetTag) {
//				byteCodeOffset = ((BytecodeOffsetTag) tag).getBytecodeOffset();
//				System.out.println("Handling byte code offset  " + byteCodeOffset);
//			}
//		}
		
		
		//SHASHIN

		// What kind of statement?
		if (stmt instanceof DefinitionStmt) {
			// Assignment of LHS to an RHS
			Value lhsOp = ((DefinitionStmt) stmt).getLeftOp();
			Value rhsOp = ((DefinitionStmt) stmt).getRightOp();

			// Invoke static initialisers if static members accessed
			// for the first time
			StaticFieldRef staticReference = null;
			if (lhsOp instanceof StaticFieldRef) {
				staticReference = ((StaticFieldRef) lhsOp);				
			} else if (rhsOp instanceof StaticFieldRef) {
				staticReference = ((StaticFieldRef) rhsOp);
			}
			if (staticReference != null) {
				SootClass declaringClass = staticReference.getField().getDeclaringClass();
				if (clinitCalled.contains(declaringClass) == false) {
					clinitCalled.add(declaringClass);
					// Don't initialise library classes
					if (declaringClass.isLibraryClass()) {
						// Set all static fields to null
						for (SootField field : declaringClass.getFields()) {
							// Only for static reference fields
							if (field.isStatic() && field.getType() instanceof RefLikeType) {
								staticHeap.setFieldSummary(PointsToGraph.GLOBAL_LOCAL, field);
							}
						}
					} else {
						// We have to initialise this class...
						if (declaringClass.declaresMethodByName("<clinit>")) {
							// Get the static initialisation method
							SootMethod clinit = declaringClass.getMethodByName("<clinit>");
							// At its entry use a blank value (with STICKY to avoid TOP termination)
							PointsToGraph clinitEntryValue =  topValue();
							clinitEntryValue.assign(PointsToGraph.STICKY_LOCAL, null);
							// Make the call!
							this.processCall(context, stmt, clinit,clinitEntryValue);
							// Do not process this statement now, wait for clinit to return
							// and this statement as a "return site"
							return null; 
						}
						// If no <clinit> defined for this class, then continue as normal :-)
					}
				}
			}
			

			// Handle statement depending on type
			if (lhsOp.getType() instanceof RefLikeType) { 
				// Both LHS and RHS are RefLikeType
				if (lhsOp instanceof InstanceFieldRef || lhsOp instanceof ArrayRef) {
					// SETFIELD
					Local lhs = (Local)(lhsOp instanceof InstanceFieldRef ?
							 	((InstanceFieldRef) lhsOp).getBase() : 
								((ArrayRef) lhsOp).getBase());
					SootField field = lhsOp instanceof InstanceFieldRef ?
							((InstanceFieldRef) lhsOp).getField() : PointsToGraph.ARRAY_FIELD;
					
					// RHS can be a local or constant (string, class, null)
					if (rhsOp instanceof Local) {
						Local rhs = (Local) rhsOp;
						out.setField(lhs, field, rhs);
					} else if (rhsOp instanceof Constant) {
						Constant rhs = (Constant) rhsOp;
						out.setFieldConstant(lhs, field, rhs);
					} else {
						throw new RuntimeException(rhsOp.toString());
					}
				} else if (rhsOp instanceof InstanceFieldRef || rhsOp instanceof ArrayRef) {
					// GETFIELD
					Local rhs = (Local)(rhsOp instanceof InstanceFieldRef ?
						 	((InstanceFieldRef) rhsOp).getBase() : 
							((ArrayRef) rhsOp).getBase());
					SootField field = rhsOp instanceof InstanceFieldRef ?
							((InstanceFieldRef) rhsOp).getField() : PointsToGraph.ARRAY_FIELD;
							
					// LHS has to be local
					if (lhsOp instanceof Local) {
						Local lhs = (Local) lhsOp;
						out.getField(lhs, rhs, field);
					} else {
						throw new RuntimeException(lhsOp.toString());
					}
				} else if (rhsOp instanceof AnyNewExpr) {
					// NEW, NEWARRAY or NEWMULTIARRAY
					AnyNewExpr anyNewExpr = (AnyNewExpr) rhsOp;
					
					
					//SHASHIN		
					SootMethod currentMethod = context.getMethod();
					
					//add each unique calling method to the calleeIndexMap calleeIndex
//					
//					int currentMethodIndex = this.methodIndex;
//					String callerMethodSig = currentMethod.getDeclaringClass().getName() + "." + currentMethod.getName();
//					
//					if(!this.calleeIndexMap.containsKey(callerMethodSig)) {
//						this.calleeIndexMap.put(callerMethodSig, methodIndex);
//						methodIndex++;
//						//currentMethodIndex = methodIndex;
//					} else {
//						currentMethodIndex = this.calleeIndexMap.get(callerMethodSig);
//					}
					//System.out.println(callerMethodSig + " " + currentMethodIndex);
					
					//SHASHIN
					
					if (lhsOp instanceof Local) {
						Local lhs = (Local) lhsOp;
						out.assignNew(lhs, anyNewExpr);
						//SHASHIN - UPDATE BCI TO NODE MAP HERE

						assert(bci != -1);
						String currentMethodSignature = getTrimmedByteCodeSignature(context.getMethod());
						assert(this.methodIndices.containsKey(currentMethodSignature));
						int currentMethodIndex = this.methodIndices.get(currentMethodSignature);
						
						this.bciMap2.put(anyNewExpr, new BciContainer(currentMethodIndex, bci));
						this.bciMap.put(anyNewExpr, currentMethodIndex + "-" + bci);
						//this.bciMap.put(anyNewExpr, currentMethodIndex + "-" + bci);
						//String methodSig = context.getMethod().getDeclaringClass().getName() + "." + context.getMethod().getName();

						//this.exprToMethodMap.put(anyNewExpr, methodSig);
						//SHASHIN
						
					} else {
						throw new RuntimeException(lhsOp.toString());						
					}
				} else if (rhsOp instanceof InvokeExpr) {
					// STATICINVOKE, SPECIALINVOKE, VIRTUALINVOKE or INTERFACEINVOKE
					InvokeExpr expr = (InvokeExpr) rhsOp;	
					// Handle method invocation!
					out = handleInvoke(context, stmt, expr, in);
				} else if (lhsOp instanceof StaticFieldRef) { 
					// Get parameters
					SootField staticField = ((StaticFieldRef) lhsOp).getField();
					// Temporarily union locals and globals
					PointsToGraph tmp = topValue();
					tmp.union(out, staticHeap);
					// Store RHS into static field
					if (rhsOp instanceof Local) {
						Local rhsLocal = (Local) rhsOp;
						tmp.setField(PointsToGraph.GLOBAL_LOCAL, staticField, rhsLocal);
					} else if (rhsOp instanceof Constant) {
						Constant rhsConstant = (Constant) rhsOp;
						tmp.setFieldConstant(PointsToGraph.GLOBAL_LOCAL, staticField, rhsConstant);
					} else {
						throw new RuntimeException(rhsOp.toString());
					}
					// Now get rid of all locals, params, etc.
					Set<Local> locals = new HashSet<Local>(tmp.roots.keySet());
					for (Local local : locals) {
						// Everything except the GLOBAL must go!
						if (local != PointsToGraph.GLOBAL_LOCAL) {
							tmp.kill(local);
						}
					}
					// Global information is updated!
					staticHeap = tmp;
					
				} else if (rhsOp instanceof StaticFieldRef) {
					// Get parameters
					Local lhsLocal = (Local) lhsOp;
					SootField staticField = ((StaticFieldRef) rhsOp).getField();
					// Temporarily union locals and globals
					PointsToGraph tmp = topValue();
					tmp.union(out, staticHeap);
					// Load static field into LHS local
					tmp.getField(lhsLocal, PointsToGraph.GLOBAL_LOCAL, staticField);
					// Now get rid of globals that we do not care about
					tmp.kill(PointsToGraph.GLOBAL_LOCAL);
					// Local information is updated!
					out = tmp;			
					
				}  else if (rhsOp instanceof CaughtExceptionRef) { 
					Local lhs = (Local) lhsOp;
					out.assignSummary(lhs);
				} else if (rhsOp instanceof IdentityRef) { 
					// Ignore identities
				} else if (lhsOp instanceof Local) {
					// Assignment
					Local lhs = (Local) lhsOp;
					// RHS op is a local, constant or class cast
					if (rhsOp instanceof Local) {
						Local rhs = (Local) rhsOp;
						out.assign(lhs, rhs);
					} else if (rhsOp instanceof Constant) {
						Constant rhs = (Constant) rhsOp;
						out.assignConstant(lhs, rhs);
					} else if (rhsOp instanceof CastExpr) { 
						Value op = ((CastExpr) rhsOp).getOp();
						if (op instanceof Local) {
							Local rhs = (Local) op;
							out.assign(lhs, rhs);
						} else if (op instanceof Constant) {
							Constant rhs = (Constant) op;
							out.assignConstant(lhs, rhs);
						} else {
							throw new RuntimeException(op.toString());
						}
					} else {
						throw new RuntimeException(rhsOp.toString());
					}
				} else {
					throw new RuntimeException(unit.toString());
				}
			} else if (rhsOp instanceof InvokeExpr) {
				// For non-reference types, only method invocations are important
				InvokeExpr expr = (InvokeExpr) rhsOp;	
				// Handle method invocation!
				out = handleInvoke(context, stmt, expr, in);
			} 

		} else if (stmt instanceof InvokeStmt) {
			// INVOKE without a return
			InvokeExpr expr = stmt.getInvokeExpr();
			// Handle method invocation!
			out = handleInvoke(context, stmt, expr, in);
		} else if (stmt instanceof ReturnStmt) {
			// Returning a value (not return-void as those are of type ReturnVoidStmt)
			Value op = ((ReturnStmt) stmt).getOp();
			Local lhs = PointsToGraph.RETURN_LOCAL;
			// We only care about reference-type returns
			if (op.getType() instanceof RefLikeType) {
				// We can return a local or a constant
				if (op instanceof Local) {
					Local rhs = (Local) op;
					out.assign(lhs, rhs);
				} else if (op instanceof Constant) {
					Constant rhs = (Constant) op;
					out.assignConstant(lhs, rhs);
				} else {
					throw new RuntimeException(op.toString());
				}				
			}
		}
		
		//SHASHIN
		//if(out != null) System.out.println(out.toString());

		return out;

	}
	
	private boolean isReflectedStatement(Stmt stmt) {
		boolean isReflected = false;
		InvokeExpr ie = null;
		
		if (stmt instanceof DefinitionStmt) {
			//we only care to test if the RHS is an operation involving reflection
			Value rhsOp = ((DefinitionStmt) stmt).getRightOp();
			if(rhsOp instanceof InvokeExpr) {
				ie = (InvokeExpr) rhsOp;
			}
			
		} else if (stmt instanceof InvokeStmt) {
			ie = stmt.getInvokeExpr();
		}
		
		if(ie != null) {
			final Scene sc = Scene.v();
	        SootMethodRef methodRef = ie.getMethodRef();
	        switch(methodRef.getDeclaringClass().getName()) {
	        	case "java.lang.reflect.Method":
	                if ("java.lang.Object invoke(java.lang.Object,java.lang.Object[])"
	                        .equals(methodRef.getSubSignature().getString())) {
	                	isReflected = true;
	                    }
	        		break;
	            case "java.lang.reflect.Constructor":
	                if ("java.lang.Object newInstance(java.lang.Object[])".equals(methodRef.getSubSignature().getString())) {
	                	System.out.println("this looks like a constructor newinstance!");
	                	isReflected = true;
	                }
	                break;
	    		default:
	    			break;
	        			
	        		
	        }
		}
		return isReflected;
	}


	/**
	 * Computes the targets of an invoke expression using a given points-to graph.
	 * 
	 * <p>For static invocations, there is only target. For instance method
	 * invocations, the targets depend on the type of receiver objects pointed-to
	 * by the instance variable whose method is being invoked.</p>
	 * 
	 * <p>If the instance variable points to a summary node, then the returned
	 * value is <tt>null</tt> signifying a <em>default</em> call-site.</p>
	 */
	private Set<SootMethod> getTargets(SootMethod callerMethod, Stmt callStmt, InvokeExpr ie, PointsToGraph ptg) {
		Set<SootMethod> targets = new HashSet<SootMethod>();
		SootMethod invokedMethod = ie.getMethod();
		String subsignature = invokedMethod.getSubSignature();
		
		// Static and special invocations refer to the target method directly
		if (ie instanceof StaticInvokeExpr || ie instanceof SpecialInvokeExpr) {
			targets.add(invokedMethod);
			return targets;
		} else {
			assert (ie instanceof InterfaceInvokeExpr || ie instanceof VirtualInvokeExpr);
			// Get the receiver
			Local receiver = (Local) ((InstanceInvokeExpr) ie).getBase();
			// Get what objects the receiver points-to
			Set<AnyNewExpr> heapNodes = ptg.getTargets(receiver);
			if (heapNodes != null) {
				// For each object, find the invoked method for the declared type
				for (AnyNewExpr heapNode : heapNodes) {
					//SHASHIN
					if(heapNode instanceof AbstractNullObj)
						continue;
					
					if (heapNode == PointsToGraph.SUMMARY_NODE) {						
						// If even one pointee is a summary node, then this is a default site
						return null;
					} else if (heapNode instanceof NewArrayExpr) {
						// Probably getClass() or something like that on an array
						return null;
					}
					// Find the top-most class that declares a method with the given
					// signature and add it to the resulting targets
					SootClass sootClass = ((RefType) heapNode.getType()).getSootClass();
					do {
						if (sootClass.declaresMethod(subsignature)) {
							targets.add(sootClass.getMethod(subsignature));
							break;
						} else if (sootClass.hasSuperclass()) {
							sootClass = sootClass.getSuperclass();
						} else {
							sootClass = null;
						}
					} while (sootClass != null);
				}
			}
			if (targets.isEmpty()) {
				// System.err.println("Warning! Null call at: " + callStmt+ " in " + callerMethod);
			}
			return targets;
		}
	}

	
	private Set<SootMethod> getDummyTarget() {
		Set<SootMethod> targets = new HashSet<SootMethod>();
		targets.add(DUMMY_METHOD);
		return targets;
	}

	/**
	 * Handles a call site by resolving the targets of the method invocation. 
	 * 
	 * The resultant flow is the union of the exit flows of all the analysed
	 * callees. If the method returns a reference-like value, this is also taken
	 * into account.
	 */
	protected PointsToGraph handleInvoke(Context<SootMethod,Unit,PointsToGraph> callerContext, Stmt callStmt,
			InvokeExpr ie, PointsToGraph in) {
		
		// Get the caller method
		SootMethod callerMethod = callerContext.getMethod();

		//SHASHIN
		/*
		//check if this invoke expression is a reflected call
		boolean isReflectInvokeExpr = false;
		Set<SootMethod> reflectedTargets = new HashSet<SootMethod>();
		
		
		final Scene sc = Scene.v();
        SootMethodRef methodRef = ie.getMethodRef();
        switch(methodRef.getDeclaringClass().getName()) {
        	case "java.lang.reflect.Method":
                if ("java.lang.Object invoke(java.lang.Object,java.lang.Object[])"
                        .equals(methodRef.getSubSignature().getString())) {

            		System.out.println("this looks like a methodinvoke!");
            		isReflectInvokeExpr = true;
            	    Set<String> ret = methodInvokeReceivers.get(callerMethod);
            	    ret = (ret != null) ? ret : Collections.emptySet();
            	    System.out.println("possible reflected targets are " + ret);
            	    for(String sig : ret) {
            	    	if(sc.containsMethod(sig)) {
            	    		System.out.println("method.Invoke target " + sig + " is resolved");
            	    		SootMethod reflectedTarget = sc.getMethod(sig);
            	    		//System.out.println(reflectedTarget.getActiveBody());
                	    	reflectedTargets.add(reflectedTarget);
            	    	}
            	    	
            	    }
                      //reflectionModel.methodInvoke(source, s);
                    }
        		break;
            case "java.lang.reflect.Constructor":
                if ("java.lang.Object newInstance(java.lang.Object[])".equals(methodRef.getSubSignature().getString())) {
                	System.out.println("this looks like a constructor newinstance!");
                	isReflectInvokeExpr = true;

            	    Set<String> ret = constructorNewInstanceReceivers.get(callerMethod);
            	    ret = (ret != null) ? ret : Collections.emptySet();
            	    for(String sig : ret) {
            	    	if(sc.containsMethod(sig)) {
            	    		System.out.println("constructor.NewInstance target " + sig + " is resolved");
            	    		SootMethod reflectedTarget = sc.getMethod(sig);
            	    		//System.out.println(reflectedTarget.getActiveBody());
                	    	reflectedTargets.add(reflectedTarget);
            	    	}
            	    	
            	    }
                  //reflectionModel.contructorNewInstance(source, s);
                }
                break;
    		default:
    			break;
        			
        		
        }
		*/
		
		//SHASHIN
		//add each unique calling method to the calleeIndexMap calleeIndex
		/*int currentCalleeIndex = this.calleeIndex;
		String callerMethodSig = callerMethod.getDeclaringClass().getName() + "." + callerMethod.getName();
		if(!this.calleeIndexMap.containsKey(callerMethodSig)) {
			this.calleeIndexMap.put(callerMethodSig, calleeIndex);
			calleeIndex++;
		} else {
			currentCalleeIndex = this.calleeIndexMap.get(callerMethodSig);
		}*/
		//SHASHIN
		
		// Initialise the final result as TOP first
		PointsToGraph resultFlow = topValue();

		// If this statement is an assignment to an object, then set LHS for
		// RETURN values.
		Local lhs = null;
		Value lhsOp = null;
		if (callStmt instanceof AssignStmt) {
			lhsOp = ((AssignStmt) callStmt).getLeftOp();
			if (lhsOp.getType() instanceof RefLikeType) {
				lhs = (Local) lhsOp;
			}
		}
		
		Set<SootMethod> targets;
		/*
		//isReflectInvokeExpr = false;
		if(isReflectInvokeExpr && !reflectedTargets.isEmpty()) {
			targets = reflectedTargets;
			//<org.dacapo.harness.TestHarness: void main(java.lang.String[])>
		}
		// Find target methods for this call site (invoke expression) using the points-to data
		else 
			*/
			targets = getTargets(callerMethod, callStmt, ie, in);

		//shashin
		if(targets != null && targets.size() == 1 && 
				(targets.iterator().next().toString().contains("<java.util.regex.Pattern") 
				|| (targets.iterator().next().toString().contains("java.util.HashMap"))))
			targets = null;
		// If "targets" is null, that means the invoking instance was SUMMARY
		// So we use the DUMMY METHOD (which is a method with no body)
		if (targets == null) {
			targets = getDummyTarget();			
			this.contextTransitions.addTransition(new CallSite<SootMethod,Unit,PointsToGraph>(callerContext, callStmt), null);
			if (verbose) {
				System.out.println("[DEF] X" + callerContext + " -> DEFAULT " + ie.getMethod());
			}
		} 
		
		// Make calls for all target methods
		/*if(targets.size() > 0)*/ this.immediatePrevContextAnalysed = true;
		for (SootMethod calledMethod : targets) {

			// The call-edge is obtained by assign para	meters and THIS, and killing caller's locals
			PointsToGraph callEdge = copy(in);
			if (calledMethod.hasActiveBody()) {
				// We need to maintain a set of locals not to kill (in case the call is recursive)
				Set<Local> doNotKill = new HashSet<Local>();
				
				// Initialise sticky parameter
				callEdge.assign(PointsToGraph.STICKY_LOCAL, null);
				doNotKill.add(PointsToGraph.STICKY_LOCAL);
				
				//System.out.println("dumping call parameters ");
				// Assign this...
				Local receiver = null;
				if (ie instanceof InstanceInvokeExpr) {
					receiver = (Local)((InstanceInvokeExpr) ie).getBase();
					//System.out.println(receiver.toString());
					//System.out.println(calledMethod.getSubSignature());
					try {
					Local thisLocal = calledMethod.getActiveBody().getThisLocal();
					
					callEdge.assign(thisLocal, receiver);
					doNotKill.add(thisLocal);
					// Sticky it!
					callEdge.assignSticky(PointsToGraph.STICKY_LOCAL, thisLocal);
					} catch (Exception ex) { }
				}
				
				//SHASHIN
				SootMethod m = (SootMethod) callerContext.getMethod();
				String mN = m.getDeclaringClass().getName() + "." + m.getName();
				String calledMethodSig = getTrimmedByteCodeSignature(calledMethod);
				//TODO: this assert does not make sense - how can a method YET to be called, already be inserted in the method indices?
//				assert(this.methodIndices.containsKey(calledMethodSig)) : calledMethodSig + " signature not found in methodIndices" ;
				//int methodIndex = this.methodIndices.get(calledMethodSig);
				//System.out.println(mN);
				//THIS MAP NOT NEEDED
				Map<Integer,Set<String>> calledMethodArgsMap;
//				if(this.callSiteInvariants.containsKey(mN)) {
//					map = this.callSiteInvariants.get(mN);
//				} else {
//					map = new HashMap<String, Map<Integer,Set<String>>>();
//				}
				
				if(this.callSiteInvariants.containsKey(calledMethodSig)) {
					calledMethodArgsMap = this.callSiteInvariants.get(calledMethodSig);
				} else {
					calledMethodArgsMap = new HashMap<Integer, Set<String>>();
				}
				
				//this map not needed anymore
//				Map<Integer, Set<String>> calledMethodArgsMap;
//				if(map.containsKey(calledMethodSig)) {
//					calledMethodArgsMap = map.get(calledMethodSig);
//				} else {
//					calledMethodArgsMap = new HashMap<Integer, Set<String>>();
//				}
				//this map not needed anymore
				
				
				Set<String> thisBCSet;
				if(calledMethodArgsMap.containsKey(0)) {
					thisBCSet = calledMethodArgsMap.get(0);
				} else {
					thisBCSet = new HashSet<String>();
				}
				
				if(receiver != null) {
					try {
					for(AnyNewExpr t : in.getTargets(receiver)) {
						if(t instanceof AbstractNullObj) continue;
						
						if(t == PointsToGraph.STRING_SITE)
							thisBCSet.add("S");
						else {
							if(this.bciMap.get(t) != null)
								thisBCSet.add(/*currentCalleeIndex + "-" + */this.bciMap.get(t).toString());
							else 
								//TODO: confirm that this is in fact the global scenario
								thisBCSet.add("G");
						
						}
					}
					} catch (Exception ex) {
						System.out.println(ex.toString());
					}
				} else {
					//this is the static scenario, modeled as a null receiver
					thisBCSet.add("N");
				}
				
				calledMethodArgsMap.put(0, thisBCSet);
//				if(calledMethodArgsMap.containsKey(0)) {
//					calledMethodArgsMap.replace(0, thisBCSet);
//				} else 
//					calledMethodArgsMap.put(0, thisBCSet);
				
				// Assign parameters...
				//if( !isReflected) if the invoke statement is reflected, do not bother about mapping parameters
				for (int i = 0; i < calledMethod.getParameterCount(); i++) {

												//SHASHIN - TODO - assert that we cannot handle non-empty args list
												Set<String> argBCSet;
												if(calledMethodArgsMap.containsKey(i + 1)) {
													argBCSet = calledMethodArgsMap.get(i + 1);
												} else {
													argBCSet = new HashSet<String>();
												}
					
					
					// Only for reference-like parameters
					
					//calledMethiod : public Avrora(Config config, File scratch)
					if (calledMethod.getParameterType(i) instanceof RefLikeType) {
						Local parameter = calledMethod.getActiveBody().getParameterLocal(i);
						Value argValue = null;
						try {
							argValue = ie.getArg(i);
						} catch (IndexOutOfBoundsException ex) {
							argValue = ie.getArg(0);
						}
						// The argument can be a constant or local, so handle accordingly
						if (argValue instanceof Local) {
							Local argLocal = (Local) argValue;
							//SHASHIN
							//System.out.println(argLocal);
							try {
							for(AnyNewExpr tgt : in.getTargets(argLocal)) {
								
													String bci;
													if(tgt == PointsToGraph.STRING_SITE) {
														bci = "S";
														argBCSet.add(bci);
													} else if (tgt instanceof AbstractNullObj) {
														argBCSet.add("N");
													} else if (tgt == PointsToGraph.SUMMARY_NODE) {
														argBCSet.add("G");
													} //note that we are not handling constants here - TODO : TEST!
													else {
														bci = this.bciMap.get(tgt);
														//TODO: revisit this assert, it doesn't seem to be trivial enough to ignore
														//assert(this.bciMap2.containsKey(tgt)) : tgt + " not located in bciMap2, caller method is " + callerContext.getMethod().toString() + ", called method is " + calledMethod.toString();
														BciContainer bciContainer = this.bciMap2.get(tgt);
														
														//String temp = this.exprToMethodMap.get(tgt);
														//int ci;
														argBCSet.add(bciContainer.getCallerIndex() + "-" + bciContainer.getBci());
//														if(temp != null && bci != null) {
//															try {
//															ci = this.calleeIndexMap.get(temp);
//															} catch (Exception ex) {
//																System.out.println(ex.toString());
//																//something wrong here, assume default value for the callee index
//																ci = -1;
//															}
//															argBCSet.add(/*ci + "-" + */bci.toString());
//														} else {
//															argBCSet.add("u-u");
//														}
								}

								//System.out.println(bci);

//								int ci = this.calleeIndexMap.get(this.exprToMethodMap.get(tgt));
//								argBCSet.add(ci + "-" + bci.toString());
								
							} } catch (Exception ex )
							{ System.out.println(ex); }
							//SHASHIN
							
							
							
							callEdge.assign(parameter, argLocal);
							doNotKill.add(parameter);
							// Sticky it!
							callEdge.assignSticky(PointsToGraph.STICKY_LOCAL, argLocal);
						} else if (argValue instanceof Constant) {
							if(argValue instanceof NullConstant)
								argBCSet.add("N");
							else if(argValue instanceof StringConstant) 
								argBCSet.add("S");
							else
								argBCSet.add("C");
							Constant argConstant = (Constant) argValue;
							callEdge.assignConstant(parameter, argConstant);
							doNotKill.add(parameter);
							// No need to sticky constants as caller does not store them anyway
						} else {
							throw new RuntimeException(argValue.toString());
						}
					}
					
					calledMethodArgsMap.put(i + 1, argBCSet);
//					if(calledMethodArgsMap.containsKey(i + 1)) {
//						calledMethodArgsMap.replace(i + 1, argBCSet);
//					} else 
//						calledMethodArgsMap.put(i + 1, argBCSet);
				}
				
//				if(map.containsKey(calledMethodSig)) {
//					map.replace(calledMethodSig, calledMethodArgsMap);
//				} else {
//					map.put(calledMethodSig, calledMethodArgsMap);
//
//				}
				
				this.callSiteInvariants.put(calledMethodSig, calledMethodArgsMap);
//				if(this.callSiteInvariants.containsKey(calledMethodSig)) {
//					this.callSiteInvariants.replace(calledMethodSig, calledMethodArgsMap);
//				} else {
//					this.callSiteInvariants.put(calledMethodSig, calledMethodArgsMap);
//
//				}
				// Kill caller data...
				for (Local callerLocal : callerMethod.getActiveBody().getLocals()) {
					if (doNotKill.contains(callerLocal) == false)
						callEdge.kill(callerLocal);
				}
				
				// There should be no "return local", but we kill it anyway (just in case)
				callEdge.kill(PointsToGraph.RETURN_LOCAL);
				
				// Create callee locals..
				for (Local calleeLocal : calledMethod.getActiveBody().getLocals()) {
					if (calleeLocal.getType() instanceof RefLikeType 
							&& doNotKill.contains(calleeLocal) == false) {
						callEdge.assign(calleeLocal, null);
					}
				}
			}
			
			// The intra-procedural edge is the IN value minus the objects from the call edge
			PointsToGraph intraEdge = copy(in);
			if (lhs != null) {
				// Oh, and remove the LHS targets too
				intraEdge.assign(lhs, null);
			}
			//intraEdge.subtractHeap(callEdge);
			
			// Value at the start of the called procedure is 
			// whatever went through the call edge
			PointsToGraph entryFlow = callEdge;
			
			

			// Make the call to this method!! (in case of body-less methods, no change)
			PointsToGraph exitFlow = calledMethod.hasActiveBody() ? 
					processCall(callerContext, callStmt, calledMethod, entryFlow) : entryFlow;

			// If the called context was analysed, exitFlow will be set, else it
			// will be null.
			if (exitFlow != null) {
				
				// Propagate stuff from called procedure's exit to the caller's return-site
				PointsToGraph returnEdge = copy(exitFlow);
				
				// Two ways to handle this:
				if (calledMethod.hasActiveBody()) {
					// Kill all the called method's locals. That's right.
					for (Local calleeLocal : calledMethod.getActiveBody().getLocals()) {
						returnEdge.kill(calleeLocal);
					}
					// Remove the stickies (so not to interfere with stickies in the intra-edge)
					// but do not collect unreachable nodes
					returnEdge.killWithoutGC(PointsToGraph.STICKY_LOCAL);					

				} 
				
				// Let's unite the intra-edge with the return edge
				PointsToGraph callOut = topValue();
				callOut.union(intraEdge, returnEdge);
				
				
				// Now we are only left with the return value, if any
				if (calledMethod.hasActiveBody()) {
					if (lhs != null) {
						callOut.assign(lhs, PointsToGraph.RETURN_LOCAL);
					}
					// Kill the @return variable whether there was an LHS or not
					callOut.kill(PointsToGraph.RETURN_LOCAL);
				} else {
					// Handle returned objects for native methods
					if (lhs != null) {
						// If a string is returned, optimise
						if (lhs.getType().equals(PointsToGraph.STRING_CONST.getType())) {
							callOut.assignConstant(lhs, PointsToGraph.STRING_CONST);
						} else if (lhs.getType().equals(PointsToGraph.CLASS_CONST.getType())) {
							callOut.assignConstant(lhs, PointsToGraph.CLASS_CONST);
						} else {
							// Have to assume the worst!
							//System.err.println("Warning! Summary node returned at " + 
							//		callStmt + " in " + callerMethod);
							callOut.assignSummary(lhs);
						}
					}
					// Also assume that all parameters are modified
					for (int i = 0; i < calledMethod.getParameterCount(); i++) {
						// Only for reference-like parameters
						if (calledMethod.getParameterType(i) instanceof RefLikeType) {
							Value argValue = ie.getArg(i);
							// Summarize if the argument is local (i.e. not a constant)
							if (argValue instanceof Local) {
								Local argLocal = (Local) argValue;
								callOut.summarizeTargetFields(argLocal);
							}
						}
					}
				}
				
				// As we may have multiple virtual calls, merge the value at OUT
				// of this target's call-site with an accumulator (resultFlow)
				resultFlow = meet(resultFlow, callOut);
			}
		}		
		
		// If at least one call succeeded, result flow is not TOP
		if (resultFlow.equals(topValue())) {
			return null;
		} else {
			return resultFlow;
		}
	}
	

	
	

	/**
	 * Returns the union of two points-to graphs.
	 */
	@Override
	public PointsToGraph meet(PointsToGraph op1, PointsToGraph op2) {
		PointsToGraph result = new PointsToGraph();
		result.union(op1, op2);
		return result;
	}

	/**
	 * The default data flow value (lattice top) is the empty points-to graph.
	 */
	@Override
	public PointsToGraph topValue() {
		return new PointsToGraph();
	}

	@Override
	public ProgramRepresentation<SootMethod, Unit> programRepresentation() {
		return DefaultJimpleRepresentation.v();
	}
}
