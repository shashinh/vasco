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
package vasco;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.Body;
import soot.Local;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AnyNewExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.options.Options;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.util.Chain;
import vasco.callgraph.CallsiteInvariantContainer;
import vasco.callgraph.PointsToAnalysis;
import vasco.callgraph.PointsToGraph;
import vasco.soot.AbstractNullObj;

/**
 * A generic forward-flow inter-procedural analysis which is fully
 * context-sensitive.
 * 
 * <p>
 * This class essentially captures a forward data flow problem which can be
 * solved using the context-sensitive inter-procedural analysis framework as
 * described in {@link InterProceduralAnalysis}.
 * </p>
 * 
 * <p>
 * This is the class that client analyses will extend in order to perform
 * forward-flow inter-procedural analysis.
 * </p>
 * 
 * @author Rohan Padhye
 * 
 * @param <M> the type of a method
 * @param <N> the type of a node in the CFG
 * @param <A> the type of a data flow value
 * 
 * @deprecated This is the old API from the initial SOAP '13 submission without
 *             call/return flow functions. It is only here for a temporary
 *             period while the {@link vasco.callgraph.PointsToAnalysis
 *             PointsToAnalysis} class is migrated to the new API. After that
 *             work is done, this class will be permanently removed from VASCO.
 */
public abstract class OldForwardInterProceduralAnalysis<M, N, A> extends InterProceduralAnalysis<M, N, A> {

	/** Constructs a new forward-flow inter-procedural analysis. */
	public OldForwardInterProceduralAnalysis() {
		// Kick-up to the super with the FORWARD direction.
		super(false);
		analysisStack = new Stack<Context<M, N, A>>();
		//loopInvariants = new HashMap<String, Map<Integer, A>>();
		loopInvariants = new HashMap<SootMethod, Map<Unit,PointsToGraph>>();
		callSiteInvariants = new HashMap<String, Map<Integer, Set<String>>>();
		calleeIndex = 1;
		methodIndex = 1;
		calleeIndexMap = new HashMap<String, Integer>();
		previousInMap = new HashMap<Integer, A>();
		//loopFixPointIndicator = new HashMap<Integer, Integer>();
		loopFixPointIndicator = new HashMap<SootMethod, Map<Integer,Integer>>();
		//loopHeaders = new HashSet<Integer>();
//		loopHeaders = new HashMap<SootMethod, Set<Integer>>();
		loopHeaders = new HashMap<SootMethod, Set<Unit>>();
		
		//initialize data structures for reflection
	    this.classForNameReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.classNewInstanceReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.constructorNewInstanceReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.methodInvokeReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.fieldSetReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.fieldGetReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    
	    this.methodIndices = new HashMap<String, Integer>();
	    this.sootMethodIndices = new HashMap<SootMethod, Integer>();
	    this.sootMethodArgs = new HashMap<SootMethod, List<Local>>();
	    this.callsiteInvariantsMap = new HashMap<SootMethod, CallsiteInvariantContainer>();
	    
	    //this.loadReflectionTrace();
	}

	protected Stack<Context<M, N, A>> analysisStack;

	public Map<String, Map<Integer, A>> loopInvariants_OLD;
	public Map<SootMethod, Map <Unit, PointsToGraph>> loopInvariants;
	public Map<String, Map<Integer, Set<String>>> callSiteInvariants;
	public Map<Integer, A> previousInMap;
	//TODO - also wrong, needs to be keyed by SootMethod
	//public Map<Integer, Integer> loopFixPointIndicator;
	public Map< SootMethod, Map<Integer, Integer>> loopFixPointIndicator;

	// consider this a unique identifier for each callee method. needed when
	// building the callsite invariants
	public Integer calleeIndex;
	public Integer methodIndex;
	// map of method name to its calleeIndex
	public Map<String, Integer> calleeIndexMap;
	//TODO - WRONG!! loop headers have to be keyed by a method!
	//public Set<Integer> loopHeaders;
	public Map <SootMethod, Set<Integer>> loopHeaders_OLD;
	public Map <SootMethod, Set<Unit>> loopHeaders;
	public boolean immediatePrevContextAnalysed;
	public boolean isCurrentInvocationSummarized;
	public boolean isInvoke = false;

	// structures to handle reflection
	protected final Map<SootMethod, Set<String>> classForNameReceivers;
	protected final Map<SootMethod, Set<String>> classNewInstanceReceivers;
	protected final Map<SootMethod, Set<String>> constructorNewInstanceReceivers;
	protected final Map<SootMethod, Set<String>> methodInvokeReceivers;
	protected final Map<SootMethod, Set<String>> fieldSetReceivers;
	protected final Map<SootMethod, Set<String>> fieldGetReceivers;
	
	public Map<String, Integer> methodIndices;
	public Map<SootMethod, Integer> sootMethodIndices;
	public Map<SootMethod, List<Local>> sootMethodArgs;
	public Map<SootMethod, CallsiteInvariantContainer> callsiteInvariantsMap;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doAnalysis() {

		// System.out.println(Scene.v().getCallGraph());

		// Initialise the MAIN context
		for (M entryPoint : programRepresentation().getEntryPoints()) {
			Context<M, N, A> context = new Context<M, N, A>(entryPoint,
					programRepresentation().getControlFlowGraph(entryPoint), false);
			A boundaryInformation = boundaryValue(entryPoint);
			initContext(context, boundaryInformation);

		}

		// Stack-of-work-lists data flow analysis.
		while (!analysisStack.isEmpty()) {
			// SHASHIN
			// print the analysis stack here
			//System.out.println("ANALYSIS STACK : " + analysisStack);
			// SHASHIN

			// Get the context at the top of the stack.
			Context<M, N, A> context = analysisStack.peek();

			//System.out.println("Context :" + context.getMethod());
			// Either analyse the next pending unit or pop out of the method
			if (!context.getWorkList().isEmpty()) {

				// SHASHIN
				// print this context's worklist here
				//System.out.println("CONTEXT WORKLIST FOR CONTEXT NO. " + context + ":" + context.getWorkList());
				// SHASHIN

				// work-list contains items; So the next unit to analyse.
				N unit = context.getWorkList().pollFirst();
				//if(unit != null && unit.toString().equals("virtualinvoke callback.<org.dacapo.harness.Callback: void start(java.lang.String)>($stack15)")) 
				//	context.getWorkList().add(unit.s);
				/*if(context.getMethod().toString().equals("<org.dacapo.harness.TestHarness: void runBenchmark(java.io.File,java.lang.String,org.dacapo.harness.TestHarness)>")) {
					
					System.out.println("TestHarness unit: " + unit + ", line number " + ((Unit) unit).getJavaSourceStartLineNumber());
				}*/
				
				if (unit != null) {
					//System.out.println("processing method: " + ( (SootMethod) context.getMethod()).getBytecodeSignature() + ", unit: " + unit.toString());
					// SHASHIN
//					try {
//						BytecodeOffsetTag bT = (BytecodeOffsetTag) ((Unit)unit).getTag("BytecodeOffsetTag");
//						int bci = bT.getBytecodeOffset();
//						System.out.println("Handling byte code offset  " + bci);
//					} catch (Exception ex) {
//						System.out.println("bci for unit is missing!");
//					}
					int unitBCI = -1;
					//System.out.println("Now processing unit: " + unit.toString());
//					try {
						BytecodeOffsetTag bT = (BytecodeOffsetTag) ((Unit) unit).getTag("BytecodeOffsetTag");
						if(bT != null)
							unitBCI = bT.getBytecodeOffset();
						//System.out.println("testing bci:  " + unitBCI);
//					} catch (Exception ex) {
//						//System.out.println("bci for unit is missing!");
//					}

					// Compute the IN data flow value (only for non-entry units).
					List<N> predecessors = context.getControlFlowGraph().getPredsOf(unit);
					if (predecessors.size() != 0) {
						// Merge all the OUT values of the predecessors
						Iterator<N> predIterator = predecessors.iterator();
						// Initialise IN to the OUT value of the first predecessor
						A in = context.getValueAfter(predIterator.next());
						// Then, merge OUT of remaining predecessors with the
						// intermediate IN value
						while (predIterator.hasNext()) {
							A predOut = context.getValueAfter(predIterator.next());
							in = meet(in, predOut);
						}
						// Set the IN value at the context
						context.setValueBefore(unit, in);
					}

					// Store the value of OUT before the flow function is processed.
					A prevOut = context.getValueAfter(unit);
					//System.out.println("\tprevOut for unit <" + unit + "> is ");
					//System.out.println(prevOut);

					// Get the value of IN
					A in = context.getValueBefore(unit);

					//System.out.println("\tIn for unit <" + unit + "> is ");
					//System.out.println(in);

					// Now perform the flow function.
					this.immediatePrevContextAnalysed = false;
					this.isCurrentInvocationSummarized = false;
					
					A out = flowFunction(context, unit, in);

					//System.out.println("\tout for unit <" + unit + "> is ");
					//System.out.println(out);

					// If the result is null, then no change
					boolean isOutNull = out == null;

					if (out == null)
						out = prevOut;

					// Set the OUT value
					context.setValueAfter(unit, out);

					// SHASHIN
					// System.out.println("OUT @" + bci + "\n" + out.toString());
					// System.out.println("PREV-OUT @" + bci + "\n" + prevOut.toString());

					// If the flow function was applied successfully and the OUT changed...

					// IF not a loop header, always add successor
//					Set<Integer> loopHeadersForMethod = this.loopHeaders.get(context.getMethod());
//					boolean isLoopHeader = loopHeadersForMethod.contains(unitBCI);

					// shashin
					/*
					 * boolean hasPreviousInChanged = false; if(isLoopHeader) {
					 * if(this.previousInMap.containsKey(unitBCI)) { var previousIn =
					 * this.previousInMap.get(unitBCI); hasPreviousInChanged =
					 * !in.equals(previousIn); } else { hasPreviousInChanged = true; }
					 * this.previousInMap.put(unitBCI, in); }
					 */
//					if (isLoopHeader)
//						//System.out.println(
//								//unitBCI + " is a loop header, and out.equals(prevOut) = " + out.equals(prevOut));
//					if (isLoopHeader && out.equals(prevOut) == false) {
//						this.loopFixPointIndicator.get(context.getMethod()).replace(unitBCI, 1);
//						//this.loopFixPointIndicator.replace(unitBCI, 1);
//					}

					// shashin

					// boolean addSucc = this.isInvoke ? !isOutNull : true;

					// immediatePrevContextAnalysed :
					// 1. stmt was an invoke
					// 2. the called context was analyzed
					if ( /*(!isLoopHeader   && immediatePrevContextAnalysed  ) || */ out.equals(prevOut) == false) {
						//System.out.println("OUT changed @" + unitBCI);

						// Then add successors to the work-list.
						for (N successor : context.getControlFlowGraph().getSuccsOf(unit)) {
							//System.out.println("ADDING TO WORKLIST FROM doAnalysis, line 228");
							context.getWorkList().add(successor);
						}
//						for (N successor : context.getControlFlowGraph().getSuccsOf(unit)) {
//							BytecodeOffsetTag bT = (BytecodeOffsetTag) ((Unit) successor).getTag("BytecodeOffsetTag");
//							if(bT != null) {
//								int succBCI = bT.getBytecodeOffset();
//								boolean isSuccessorLoopHeader = this.loopHeaders.get(context.getMethod()).contains(succBCI);
//								if(isSuccessorLoopHeader) {
//									boolean isFixpointReached = this.loopFixPointIndicator.get(context.getMethod()).get(succBCI) == 2;
//									if(!isFixpointReached)
//										context.getWorkList().add(successor);
//								}
//							}
//						}
						// If the unit is in TAILS, then we have at least one
						// path to the end of the method, so add the NULL unit
						if (context.getControlFlowGraph().getTails().contains(unit)) {
							//System.out.println("ADDING null TO WORKLIST FROM doAnalysis, line 234");
							context.getWorkList().add(null);
						}
					}

					// this.isInvoke = false;
					// this.immediatePrevContextAnalysed = true;

					// SHASHIN
//					else {
//						//if(context.isAnalysed()) {
//							System.out.println("OUT did not change!");
//							System.out.println(unitBCI + ":");
//							System.out.println(out.toString());
//						//}
//					}

//					if (isLoopHeader && out.equals(prevOut)) {
//
//						this.loopFixPointIndicator.get(context.getMethod()).replace(unitBCI, 2);
//						//this.loopFixPointIndicator.replace(unitBCI, 2);
//
//						SootMethod m = (SootMethod) context.getMethod();
//						//String mN = m.getDeclaringClass().getName() + "." + m.getName();
//						String methodSig = getTrimmedByteCodeSignature(m);
//						Map<Integer, A> map;
//						if (this.loopInvariants.containsKey(methodSig)) {
//							map = this.loopInvariants.get(methodSig);
//						} else {
//							map = new HashMap<Integer, A>();
//						}
//						A ptgToAdd;
//						if (map.containsKey(unitBCI)) {
//							ptgToAdd = meet(map.get(unitBCI), out);
//						} else {
//							ptgToAdd = out;
//						}
//						map.put(unitBCI, ptgToAdd);
//						this.loopInvariants.put(methodSig, map);
////						if (this.loopInvariants.containsKey(mN))
////							this.loopInvariants.replace(mN, map);
////						else
////							this.loopInvariants.put(mN, map);
////						System.out.println("*******");
////						System.out.println("Loop Invariant PTG @" + unitBCI);
////						System.out.println(out.toString());
////						System.out.println("*******");
//					}

//					if(isLoopHeader && this.loopFixPointIndicator.get(unitBCI) != 2) {
//						System.out.println("ADDING TO WORKLIST FROM doAnalysis, line 276");
//						context.getWorkList().add(unit);
//					}

					//System.out.println("running PTG @" + unitBCI + ":");
					//System.out.println(out.toString());

					// SHASHIN

				} else {
					// NULL unit, which means the end of the method.
					assert (context.getWorkList().isEmpty());

					// Exit flow value is the merge of the OUTs of the tail nodes.
					A exitFlow = topValue();
					for (N tail : context.getControlFlowGraph().getTails()) {
						A tailOut = context.getValueAfter(tail);
						exitFlow = meet(exitFlow, tailOut);
					}
					// Set the exit flow of the context.
					//boolean shouldAnalyzeCallers = !exitFlow.equals(context.getExitValue);
					context.setExitValue(exitFlow);

					// Mark this context as analysed at least once.
					context.markAnalysed();
					
					//before we cleanup the the context, lets save away the loop invariants
					processLoopInvariants(context);

					// Add return nodes to stack (only if there were callers).
//					Set<CallSite<M, N, A>> callersSet = contextTransitions.getCallers(context);
					
					//Add ALL callers of this method, irrespective of context
					// if shouldAnalyzeCallers {
						Set<CallSite <M, N, A>> callersSet = contextTransitions.getContextInsensitiveCallersForMethod(context.getMethod());
						if (callersSet != null) {
							List<CallSite<M, N, A>> callers = new LinkedList<CallSite<M, N, A>>(callersSet);
							// Sort the callers in ascending order of their ID so that
							// the largest ID is on top of the stack
							Collections.sort(callers);
							for (CallSite<M, N, A> callSite : callers) {
								// Extract the calling context and unit from the caller site.
								Context<M, N, A> callingContext = callSite.getCallingContext();
								N callingNode = callSite.getCallNode();
								// Add the calling unit to the calling context's work-list.
								//System.out.println("ADDING TO WORKLIST FROM doAnalysis, line 314");
								callingContext.getWorkList().add(callingNode);
								// Ensure that the calling context is on the analysis stack,
								// and if not, push it on to the stack.
								if (!analysisStack.contains(callingContext)) {
									analysisStack.push(callingContext);
								}
								
	//							analysisStack.remove(callingContext);
	//							analysisStack.push(callingContext);
							}
						}
					// } end if shouldAnalyzeCallers

					// Free memory on-the-fly if not needed
					if (freeResultsOnTheFly) {
						Set<Context<M, N, A>> reachableContexts = contextTransitions.reachableSet(context, true);
						// If any reachable contexts exist on the stack, then we cannot free memory
						boolean canFree = true;
						for (Context<M, N, A> reachableContext : reachableContexts) {
							if (analysisStack.contains(reachableContext)) {
								canFree = false;
								break;
							}
						}
						// If no reachable contexts on the stack, then free memory associated
						// with this context
						if (canFree) {
							for (Context<M, N, A> reachableContext : reachableContexts) {
								reachableContext.freeMemory();
							}
						}
					}
				}
				this.isCurrentInvocationSummarized = false;
			} else {
				// If work-list is empty, then remove it from the analysis.
				analysisStack.remove(context);
			}
		}

		// Sanity check
		for (List<Context<M, N, A>> contextList : contexts.values()) {
			for (Context<M, N, A> context : contextList) {
				if (context.isAnalysed() == false) {
					System.err.println(
							"*** ATTENTION ***: Only partial analysis of X" + context + " " + context.getMethod());
				}
			}
		}
		
		//TODO: Shashin - insert callsite invariant logic right here
		processCallsiteInvariants();
		
	}
	private void processLoopInvariants(Context <M, N, A> context) {
		
		PointsToAnalysis pta = (PointsToAnalysis) this;
		SootMethod method = (SootMethod) context.getMethod();
		Set<Unit> loopHeaders = this.loopHeaders.get(method);
		Map<Unit, PointsToGraph> invariantsForMethod = this.loopInvariants.getOrDefault(method, new HashMap<Unit, PointsToGraph>());
		for(Unit loopHeader : loopHeaders) {
			
			BytecodeOffsetTag bT = (BytecodeOffsetTag) loopHeader.getTag("BytecodeOffsetTag");
			if(bT != null) {
				PointsToGraph loopInvariantForHeader = invariantsForMethod.getOrDefault(loopHeader, pta.topValue());
				if(context.isAnalysed()) {
					PointsToGraph outForHeader = (PointsToGraph) context.getValueAfter((N) loopHeader);
					loopInvariantForHeader = pta.meet(loopInvariantForHeader, outForHeader);
				}
				invariantsForMethod.put(loopHeader, loopInvariantForHeader);
			}
		}
		
		this.loopInvariants.put(method, invariantsForMethod);
		
		
//		for(SootMethod method : pta.contexts.keySet()) {
//			List<Context<SootMethod, Unit, PointsToGraph>> contextsForMethod = pta.getContexts(method);
//			Set<Unit> loopHeaders = this.loopHeaders.get(method);
//			
//			Map<Unit, PointsToGraph> invariantsForMethod = new HashMap<Unit, PointsToGraph>();
//			
//			for(Unit loopHeader : loopHeaders) {
//				PointsToGraph loopInvariantForHeader = pta.topValue();
//				for (Context <SootMethod, Unit, PointsToGraph> context : contextsForMethod) {
//					if(context.isAnalysed()) {
//						PointsToGraph outForHeader = context.getValueAfter(loopHeader);
//						loopInvariantForHeader = pta.meet(loopInvariantForHeader, outForHeader);
//					}
//				}
//				invariantsForMethod.put(loopHeader, loopInvariantForHeader);
//			}
//			
//			this.loopInvariants.put(method, invariantsForMethod);
//		}
	}
	
	
	private void processCallsiteInvariants() {
		PointsToAnalysis pta = (PointsToAnalysis) this;
		for(SootMethod m : this.sootMethodIndices.keySet()) {
			List<Context <SootMethod, Unit, PointsToGraph>> contextsForMethod = pta.getContexts(m);
			PointsToGraph aggregate = pta.topValue();
			
			for(Context <SootMethod, Unit, PointsToGraph> context : contextsForMethod) {
				aggregate = pta.meet(aggregate, context.getEntryValue());
			}
			
//			if (m.getBytecodeSignature().equals("<B: foo()LD;>")) {
//				System.out.println(aggregate);
				
			Map<Integer, Local> paramLocalsForMethod = new HashMap<Integer, Local>();
			if(! m.isStatic()) {
				//method is non-static, valid this-param exists
				Local thisLocal = m.getActiveBody().getThisLocal();
				paramLocalsForMethod.put(0, thisLocal);
			} /* else paramLocalsForMethod.put(0, null);*/
			
			for(int i = 0; i < m.getParameterCount(); i++) {
				if(m.getParameterType(i) instanceof RefLikeType) {
					Local parameterLocal = m.getActiveBody().getParameterLocal(i);
					paramLocalsForMethod.put(i+1, parameterLocal);
				}
			}
			
			CallsiteInvariantContainer ciContainer = new CallsiteInvariantContainer(paramLocalsForMethod, aggregate);
			
			this.callsiteInvariantsMap.put(m, ciContainer);
			
//					for(Local paramLocal : paramLocalsForMethod) {
//						
//						Set<AnyNewExpr> targets = aggregate.getTargets(paramLocal);
//						for(AnyNewExpr target : targets) {
//							if(target == PointsToGraph.STRING_SITE) {
//								
//							} else if (target instanceof NewArrayExpr) {
//								
//							} else if (target instanceof AbstractNullObj) {
//								
//							} else if (target == PointsToGraph.SUMMARY_NODE) {
//								
//							} else if (target == PointsToGraph.CLASS_SITE) {
//								
//							}
//							else {
//								assert(pta.bciMap2.containsKey(target));
//								System.out.println(pta.bciMap2.get(target));
//							}
//							
//						}
//					}
//			}
			
		}
	}
	public String getTrimmedByteCodeSignature(SootMethod m) {
		String methodSig = m.getBytecodeSignature();
		String sig = methodSig.replace(".", "/").replace(": ", ".").substring(1, methodSig.length() - 2);
		return sig;
	}
	/**
	 * Creates a new context and initialises data flow values.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ol>
	 * <li>Initialise all nodes to default flow value (lattice top).</li>
	 * <li>Initialise the entry nodes (heads) with a copy of the entry value.</li>
	 * <li>Add entry points to work-list.</li>
	 * <li>Push this context on the top of the analysis stack.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param context    the context to initialise
	 * @param entryValue the data flow value at the entry of this method
	 */
	protected void initContext(Context<M, N, A> context, A entryValue) {
		// Get the method
		M method = context.getMethod();

		// First initialise all points to default flow value.
		for (N unit : context.getControlFlowGraph()) {
			context.setValueBefore(unit, topValue());
			context.setValueAfter(unit, topValue());
		}

		// Now, initialise entry points with a copy of the given entry flow.
		context.setEntryValue(copy(entryValue));
		for (N unit : context.getControlFlowGraph().getHeads()) {
			context.setValueBefore(unit, copy(entryValue));
			// Add entry points to work-list
			//System.out.println("ADDING TO WORKLIST FROM initContext, line 392");
			context.getWorkList().add(unit);
		}

		// Add this new context to the given method's mapping.
		if (!contexts.containsKey(method)) {
			contexts.put(method, new LinkedList<Context<M, N, A>>());
		}
		contexts.get(method).add(context);

		// Push this context on the top of the analysis stack.
		analysisStack.add(context);

		// SHASHIN

		SootMethod sMethod = (SootMethod) context.getMethod();
		String sig = getTrimmedByteCodeSignature(sMethod);
		int index = this.methodIndices.size();
		//maintain an index for each unique method signature
		if(! this.methodIndices.containsKey(sig)) {
			this.methodIndices.put(sig, ++index);
		}

		int index2 = this.sootMethodIndices.size();
		//maintain an index for each unique method signature
		if(! this.sootMethodIndices.containsKey(sMethod)) {
			this.sootMethodIndices.put(sMethod, ++index2);
		}
		
		if(! this.sootMethodArgs.containsKey(sMethod)) {
			List<Local> refParamLocals = new ArrayList<Local>();
			if(! sMethod.isStatic()) {
				//method is non-static, valid this-param exists
				Local thisLocal = sMethod.getActiveBody().getThisLocal();
				refParamLocals.add(thisLocal);
			}
			
			for(int i = 0; i < sMethod.getParameterCount(); i++) {
				if(sMethod.getParameterType(i) instanceof RefLikeType) {
					Local parameterLocal = sMethod.getActiveBody().getParameterLocal(i);
					refParamLocals.add(parameterLocal);
				}
			}
			
			this.sootMethodArgs.put(sMethod, refParamLocals);
		}
		//System.out.println(methodName);

		// fetch the loop headers

//		LoopFinder lf = new LoopFinder();
//		Body b = sMethod.getActiveBody();
//		Chain<Local> locals = b.getLocals();
//		//System.out.println(b.toString());
//		lf.transform(b);
//		Set <Integer> loopHeaders = new HashSet<Integer>();
//		Map<Integer, Integer> loopFixPointIndicator = new HashMap<Integer, Integer> ();
//		Set<Loop> loops = lf.getLoops(b);
//		for (Loop l : loops) {
//			BytecodeOffsetTag bt = (BytecodeOffsetTag) l.getHead().getTag("BytecodeOffsetTag");
//			if (bt == null) {
//				System.out.println("bt is null!");
//				System.err.println("a loop header's BytecodeOffsetTag is missing!");
//			} else {
//				int bci = bt.getBytecodeOffset();
//				// dirty hack alert!
//				// if(bci != 0) {
//				loopHeaders.add(bci);
//				loopFixPointIndicator.put(bci, 0);
//				//System.out.println(bci + " is a loop header!");
//				// }
//			}
//
//		}
//		this.loopFixPointIndicator.put(sMethod, loopFixPointIndicator);
//		this.loopHeaders.put(sMethod, loopHeaders);

		if(! this.loopHeaders.containsKey(sMethod)) {
			LoopFinder lf = new LoopFinder();
			Body b = sMethod.getActiveBody();
			Chain<Local> locals = b.getLocals();
			//System.out.println(b.toString());
			lf.transform(b);
			Set <Unit> loopHeaders = new HashSet<Unit>();
			Set<Loop> loops = lf.getLoops(b);
			for (Loop l : loops) {
				Stmt loopHead = l.getHead();
				loopHeaders.add((Unit) loopHead);
			}
			this.loopHeaders.put(sMethod, loopHeaders);
			
//			System.out.println("******method loop headers -" + sMethod.getBytecodeSignature() + " ***********");
//			System.out.println(loopHeaders);
		}
		
		//debugging - print each unit and its bci
//		System.out.println("******method bci and units -" + sMethod.getBytecodeSignature() + " ***********");
//		for(Unit u : b.getUnits()) {
//			BytecodeOffsetTag bt = (BytecodeOffsetTag) u.getTag("BytecodeOffsetTag");
//			if(bt != null) {
//				System.out.println(bt.getBytecodeOffset() + " : " + u.toString());
//			}
//			
//		}
//		System.out.println("******method bci and units***********");
		// SHASHIN
	}

	/**
	 * Processes a call statement.
	 * 
	 * <p>
	 * Retrieves a value context for the callee if one exists with the given entry
	 * value, or else creates a new one and adds the transition to the context
	 * transition table.
	 * </p>
	 * 
	 * <p>
	 * If the callee context has already been analysed, returns the resulting exit
	 * value. For newly created contexts the result would be <tt>null</tt>, as they
	 * are obviously not analysed even once.
	 * </p>
	 * 
	 * <p>
	 * Note that this method is not directly called by {@link #doAnalysis()
	 * doAnalysis}, but is instead called by
	 * {@link #flowFunction(Context, Object, Object) flowFunction} when a method
	 * call statement is encountered. The reason for this design decision is that
	 * client analyses may want their own setup and tear down sequences before a
	 * call is made (similar to edge flow functions at the call and return site).
	 * Also, analyses may want to choose which method call to process at an invoke
	 * statement in the case of virtual calls (e.g. a points-to analysis may build
	 * the call-graph on-the-fly).
	 * </p>
	 * 
	 * <p>
	 * Therefore, it is the responsibility of the client analysis to detect an
	 * invoke expression when implementing
	 * {@link #flowFunction(Context, Object, Object) flowFunction}, and suitably
	 * invoke {@link #processCall(Context, Object, Object, Object) processCall} with
	 * the input data flow value which may be different from the IN/OUT value of the
	 * node due to handling of arguments, etc. Similarly, the result of
	 * {@link #processCall(Context, Object, Object, Object) processCall} may be
	 * modified by the client to handle return values, etc. before returning from
	 * {@link #flowFunction(Context, Object, Object) flowFunction}. Ideally,
	 * {@link #flowFunction(Context, Object, Object) flowFunction} should return
	 * <tt>null</tt> if and only if
	 * {@link #processCall(Context, Object, Object, Object) processCall} returns
	 * <tt>null</tt>.
	 * 
	 * @param callerContext the analysis context at the call-site
	 * @param callNode      the calling statement
	 * @param method        the method being called
	 * @param entryValue    the data flow value at the entry of the called method.
	 * @return the data flow value at the exit of the called method, if available,
	 *         or <tt>null</tt> if unavailable.
	 */
	protected A processCall(Context<M, N, A> callerContext, N callNode, M method, A entryValue) {
		/*
		 * TODO: to keep things in sync with our JITC components, we want to analyze methods context insensitively.
		 * 		
		 * basically, check if the current entry value is subsumed by the entry value in the context already analyzed,
		 * if so - then no change.
		 * but if not -instead of creating a new context below, perform a meet of the entry value with the entry value in the (singleton) context of the method
		 * 
		 */

		CallSite<M, N, A> callSite = new CallSite<M, N, A>(callerContext, callNode);

		// Check if the called method has a context associated with this entry flow:
		Context<M, N, A> calleeContext = getContext(method, entryValue);
		// If not, then set 'calleeContext' to a new context with the given entry flow.
		if (calleeContext == null) {
			//previously - this context is not analyzed/first time analyzed
			//calleeContext = mergeContexts(thisContext, getContexts(method))
			//assert (calleeContext.isAnalysed || hasChanged == false);
			
			calleeContext = new Context<M, N, A>(method, programRepresentation().getControlFlowGraph(method), false);
			initContext(calleeContext, entryValue);
			if (verbose) {
				System.out.println("[NEW] X" + callerContext + " -> X" + calleeContext + " " + method + " ");
			}
		}

		// Store the transition from the calling context and site to the called context.
		contextTransitions.addTransition(callSite, calleeContext);

		// Check if 'caleeContext' has been analysed (surely not if it is just newly
		// made):
		if (calleeContext.isAnalysed()) {
			if (verbose) {
				System.out.println("[HIT] X" + callerContext + " -> X" + calleeContext + " " + method + " ");
			}
			// If yes, then return the 'exitFlow' of the 'calleeContext'.
			return calleeContext.getExitValue();
		} else {
			// If not, then return 'null'.
			return null;
		}
	}

	/*
	 * Context-Insensitive variant of processCall() above
	 * 
	 */
	protected A processCallContextInsensitive(Context<M, N, A> callerContext, N callNode, M method, A entryValue) {
		
		System.out.println("processCallContextInsensitive : " + method.toString());
		
		//fetch the context(s) associated with this method
		List< Context<M, N, A> > calleeContexts = getContexts(method);
		
		CallSite <M, N, A> callSite = new CallSite<M, N, A>(callerContext, callNode);
		
		Context <M, N, A> calleeContext;
		
		if(calleeContexts.size() == 0) {
			//this method has no contexts yet, i.e. it has never been analyzed
			//create a new context and add a transition
			
			calleeContext = new Context <M, N, A> (method, programRepresentation().getControlFlowGraph(method), false);
			initContext(calleeContext, entryValue);
			if(verbose) {
				System.out.println("[NEW] X" + callerContext + " -> X" + calleeContext + " " + method + " ");
			}
			
		} else {
			//a callee context for this method exists. Fetch its entry value, merge it with the incoming entryvalue, and add it back to the worklist

			//if context-insensitive, there should be at most one context for this method
			assert(calleeContexts.size() == 1);

			//this is safe, because we have asserted that callContexts is not null and has a single entry
			calleeContext = calleeContexts.get(0);
			
			entryValue = meet(entryValue, calleeContext.getEntryValue());
			
			//now see if a context with this entryValue already exists
			Context <M, N, A> contextInsensitiveContext = getContext(method, entryValue);
			if(contextInsensitiveContext == null) {
				/*
				 * the method hasn't been analyzed with this aggregated entry value
				 * create a new context with this entry value, remove the old one, and add the new one
				 */
				
				// oldExitFlow = calleeContext.getExitValue();
				calleeContext = new Context <M, N, A> (method, programRepresentation().getControlFlowGraph(method), false);
				// calleeContext.setExitValue(oldExitFlow);
				//clears all contexts associated with this method
				this.contexts.remove(method);
				//also remove it from the worklist, if present; should not be. Commenting out.
				//this.worklist.remove(calleeContext);
				
				//adds in the new context for analysis
				initContext(calleeContext, entryValue);
			}
			
		}
		
		//TODO: make this context insensitive. instead of calleecontext, make the map the method name
		// essentially, when we do get callers, we want ALL callsites later - not just the callsites that called with this specific context
		//contextTransitions.addTransition(callSite, calleeContext);
		contextTransitions.addContextInsensitiveCaller(callSite, method);
		//simpler way - maintain a map of callsites, to the called methods
		//then later, when a method is marked analyzed - simply fetch all its callers irrespective of context and add them to the worklist (BOOM !)
		
		// Check if 'caleeContext' has been analysed (surely not if it is just newly
		// made):
		if (calleeContext.isAnalysed()) {
			if (verbose) {
				System.out.println("[HIT] X" + callerContext + " -> X" + calleeContext + " " + method + " ");
			}
			// If yes, then return the 'exitFlow' of the 'calleeContext'.
			return calleeContext.getExitValue();
		} else {
			// If not, then return 'null'.
			return null;
		}
		
	}

	protected abstract A flowFunction(Context<M, N, A> context, N unit, A in);

}
