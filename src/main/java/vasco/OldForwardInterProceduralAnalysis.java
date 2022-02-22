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
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AnyNewExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.options.Options;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.util.Chain;
import vasco.callgraph.PointsToGraph;

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
		loopInvariants = new HashMap<String, Map<Integer, A>>();
		callSiteInvariants = new HashMap<String, Map<Integer, Set<String>>>();
		calleeIndex = 1;
		methodIndex = 1;
		calleeIndexMap = new HashMap<String, Integer>();
		previousInMap = new HashMap<Integer, A>();
		loopFixPointIndicator = new HashMap<Integer, Integer>();
		loopHeaders = new HashSet<Integer>();
		
		//initialize data structures for reflection
	    this.classForNameReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.classNewInstanceReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.constructorNewInstanceReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.methodInvokeReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.fieldSetReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    this.fieldGetReceivers = new LinkedHashMap<SootMethod, Set<String>>();
	    
	    //this.loadReflectionTrace();
	}

	protected Stack<Context<M, N, A>> analysisStack;

	public Map<String, Map<Integer, A>> loopInvariants;
	public Map<String, Map<Integer, Set<String>>> callSiteInvariants;
	public Map<Integer, A> previousInMap;
	public Map<Integer, Integer> loopFixPointIndicator;

	// consider this a unique identifier for each callee method. needed when
	// building the callsite invariants
	public Integer calleeIndex;
	public Integer methodIndex;
	// map of method name to its calleeIndex
	public Map<String, Integer> calleeIndexMap;
	public Set<Integer> loopHeaders;
	public boolean immediatePrevContextAnalysed;
	public boolean isInvoke = false;

	// structures to handle reflection
	protected final Map<SootMethod, Set<String>> classForNameReceivers;
	protected final Map<SootMethod, Set<String>> classNewInstanceReceivers;
	protected final Map<SootMethod, Set<String>> constructorNewInstanceReceivers;
	protected final Map<SootMethod, Set<String>> methodInvokeReceivers;
	protected final Map<SootMethod, Set<String>> fieldSetReceivers;
	protected final Map<SootMethod, Set<String>> fieldGetReceivers;

	private void loadReflectionTrace() {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		//String logFile = "test-classes/refl02/out/refl.log";
		//String logFile = "test-classes/test43/out/refl.log";
		String logFile = "test-classes/dacapo/refl.log";
		
		//-cp .:test-classes/refl02/out  -reflog test-classes/refl02/out/refl.log -out test-classes/refl02/results Main
		//-cp .:test-classes/refl03/out  -reflog test-classes/refl03/out/refl.log -out test-classes/refl03/results Main
		//-cp .:test-classes/dacapo -out test-classes/dacapo/results -reflog test-classes/dacapo/refl.log Harness
		
		final Options opts = Options.v();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)))) {
	        final Scene sc = Scene.v();

	        final Set<String> ignoredKinds = new HashSet<String>();
			for (String line; (line = reader.readLine()) != null;) {
				if (line.isEmpty()) {
					continue;
				}

				final String[] portions = line.split(";", -1);
				final String kind = portions[0];
				final String target = portions[1];
				final String source = portions[2];
				final int lineNumber = portions[3].length() == 0 ? -1 : Integer.parseInt(portions[3]);

				for (SootMethod sourceMethod : inferSource(source, lineNumber)) {
					switch (kind) {
					case "Class.forName": {
						Set<String> receiverNames = classForNameReceivers.get(sourceMethod);
						if (receiverNames == null) {
							classForNameReceivers.put(sourceMethod, receiverNames = new LinkedHashSet<String>());
						}
						receiverNames.add(target);
						break;
					}
					case "Class.newInstance": {
						Set<String> receiverNames = classNewInstanceReceivers.get(sourceMethod);
						if (receiverNames == null) {
							classNewInstanceReceivers.put(sourceMethod, receiverNames = new LinkedHashSet<String>());
						}
						receiverNames.add(target);
						break;
					}
					case "Method.invoke": {
						if (!sc.containsMethod(target)) {
							throw new RuntimeException("Unknown method for signature: " + target);
						}
						Set<String> receiverNames = methodInvokeReceivers.get(sourceMethod);
						if (receiverNames == null) {
							methodInvokeReceivers.put(sourceMethod, receiverNames = new LinkedHashSet<String>());
						}
						receiverNames.add(target);
						break;
					}
					case "Constructor.newInstance": {
						if (!sc.containsMethod(target)) {
							throw new RuntimeException("Unknown method for signature: " + target);
						}
						Set<String> receiverNames = constructorNewInstanceReceivers.get(sourceMethod);
						if (receiverNames == null) {
							constructorNewInstanceReceivers.put(sourceMethod,
									receiverNames = new LinkedHashSet<String>());
						}
						receiverNames.add(target);
						break;
					}
					case "Field.set*": {
						if (!sc.containsField(target)) {
							throw new RuntimeException("Unknown method for signature: " + target);
						}
						Set<String> receiverNames = fieldSetReceivers.get(sourceMethod);
						if (receiverNames == null) {
							fieldSetReceivers.put(sourceMethod, receiverNames = new LinkedHashSet<String>());
						}
						receiverNames.add(target);
						break;
					}
					case "Field.get*": {
						if (!sc.containsField(target)) {
							throw new RuntimeException("Unknown method for signature: " + target);
						}
						Set<String> receiverNames = fieldGetReceivers.get(sourceMethod);
						if (receiverNames == null) {
							fieldGetReceivers.put(sourceMethod, receiverNames = new LinkedHashSet<String>());
						}
						receiverNames.add(target);
						break;
					}
					default:
						ignoredKinds.add(kind);
						break;
					}

				}
			}

		} catch (FileNotFoundException e) {
			throw new RuntimeException("Trace file not found.", e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<SootMethod> inferSource(String source, int lineNumber) {
		int dotIndex = source.lastIndexOf('.');
		String className = source.substring(0, dotIndex);
		String methodName = source.substring(dotIndex + 1);
		final Scene scene = Scene.v();

		if (!scene.containsClass(className)) {
			//scene.addBasicClass(className, SootClass.BODIES);
			//scene.loadBasicClasses();
			if (!scene.containsClass(className)) {
				throw new RuntimeException("Trace file refers to unknown class: " + className);
			}
		}

		Set<SootMethod> methodsWithRightName = new LinkedHashSet<SootMethod>();
		for (SootMethod m : scene.getSootClass(className).getMethods()) {
			if (m.isConcrete() && m.getName().equals(methodName)) {
				methodsWithRightName.add(m);
			}
		}

		if (methodsWithRightName.isEmpty()) {
			throw new RuntimeException(
					"Trace file refers to unknown method with name " + methodName + " in Class " + className);
		} else if (methodsWithRightName.size() == 1) {
			return Collections.singleton(methodsWithRightName.iterator().next());
		} else {
			// more than one method with that name
			for (SootMethod sootMethod : methodsWithRightName) {
				if (coversLineNumber(lineNumber, sootMethod)) {
					return Collections.singleton(sootMethod);
				}
				if (sootMethod.isConcrete()) {
					if (!sootMethod.hasActiveBody()) {
						sootMethod.retrieveActiveBody();
					}
					Body body = sootMethod.getActiveBody();
					if (coversLineNumber(lineNumber, body)) {
						return Collections.singleton(sootMethod);
					}
					for (Unit u : body.getUnits()) {
						if (coversLineNumber(lineNumber, u)) {
							return Collections.singleton(sootMethod);
						}
					}
				}
			}

			// if we get here then we found no method with the right line number
			// information;
			// be conservative and return all method that we found
			return methodsWithRightName;
		}
	}

	private boolean coversLineNumber(int lineNumber, Host host) {
		{
			SourceLnPosTag tag = (SourceLnPosTag) host.getTag(SourceLnPosTag.IDENTIFIER);
			if (tag != null) {
				if (tag.startLn() <= lineNumber && tag.endLn() >= lineNumber) {
					return true;
				}
			}
		}
		{
			LineNumberTag tag = (LineNumberTag) host.getTag(LineNumberTag.IDENTIFIER);
			if (tag != null) {
				if (tag.getLineNumber() == lineNumber) {
					return true;
				}
			}
		}
		return false;
	}

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
				if(context.getMethod().toString().equals("<org.dacapo.harness.TestHarness: void runBenchmark(java.io.File,java.lang.String,org.dacapo.harness.TestHarness)>")) {
					
					System.out.println("TestHarness unit: " + unit + ", line number " + ((Unit) unit).getJavaSourceStartLineNumber());
				}
				
				if (unit != null) {
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
					try {
						BytecodeOffsetTag bT = (BytecodeOffsetTag) ((Unit) unit).getTag("BytecodeOffsetTag");
						unitBCI = bT.getBytecodeOffset();
						//System.out.println("testing bci:  " + unitBCI);
					} catch (Exception ex) {
						//System.out.println("bci for unit is missing!");
					}

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
					
					//TODO: if the unit being analyzed is a reflective call, then simply bypass this and  set out = in.
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
					boolean isLoopHeader = this.loopHeaders.contains(unitBCI);

					// shashin
					/*
					 * boolean hasPreviousInChanged = false; if(isLoopHeader) {
					 * if(this.previousInMap.containsKey(unitBCI)) { var previousIn =
					 * this.previousInMap.get(unitBCI); hasPreviousInChanged =
					 * !in.equals(previousIn); } else { hasPreviousInChanged = true; }
					 * this.previousInMap.put(unitBCI, in); }
					 */
					if (isLoopHeader)
						//System.out.println(
								//unitBCI + " is a loop header, and out.equals(prevOut) = " + out.equals(prevOut));
					if (isLoopHeader && out.equals(prevOut) == false)
						this.loopFixPointIndicator.replace(unitBCI, 1);

					// shashin

					// boolean addSucc = this.isInvoke ? !isOutNull : true;

					// immediatePrevContextAnalysed :
					// 1. stmt was an invoke
					// 2. the called context was analyzed
					if ( (!isLoopHeader && immediatePrevContextAnalysed) || out.equals(prevOut) == false) {
						//System.out.println("OUT changed @" + unitBCI);

						// Then add successors to the work-list.
						for (N successor : context.getControlFlowGraph().getSuccsOf(unit)) {
							//System.out.println("ADDING TO WORKLIST FROM doAnalysis, line 228");
							context.getWorkList().add(successor);
						}
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

					if (isLoopHeader && out.equals(prevOut)) {

						this.loopFixPointIndicator.replace(unitBCI, 2);

						SootMethod m = (SootMethod) context.getMethod();
						String mN = m.getDeclaringClass().getName() + "." + m.getName();
						Map<Integer, A> map = new HashMap<Integer, A>();
						if (this.loopInvariants.containsKey(mN)) {
							map = this.loopInvariants.get(mN);
						}
						A ptgToAdd;
						if (map.containsKey(unitBCI)) {
							ptgToAdd = meet(map.get(unitBCI), out);
						} else {
							ptgToAdd = out;
						}
						map.put(unitBCI, ptgToAdd);
						if (this.loopInvariants.containsKey(mN))
							this.loopInvariants.replace(mN, map);
						else
							this.loopInvariants.put(mN, map);
//						System.out.println("*******");
//						System.out.println("Loop Invariant PTG @" + unitBCI);
//						System.out.println(out.toString());
//						System.out.println("*******");
					}

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
					context.setExitValue(exitFlow);

					// Mark this context as analysed at least once.
					context.markAnalysed();

					// Add return nodes to stack (only if there were callers).
					Set<CallSite<M, N, A>> callersSet = contextTransitions.getCallers(context);
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
						}
					}

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
		String methodName = sMethod.getDeclaringClass().getName() + "." + sMethod.getName();
		//System.out.println(methodName);

		// fetch the loop headers

		LoopFinder lf = new LoopFinder();
		Body b = sMethod.getActiveBody();
		Chain<Local> locals = b.getLocals();
		//System.out.println(b.toString());
		lf.transform(b);
		Set<Loop> loops = lf.getLoops(b);
		for (Loop l : loops) {
			BytecodeOffsetTag bt = (BytecodeOffsetTag) l.getHead().getTag("BytecodeOffsetTag");
			if (bt == null) {
				System.out.println("bt is null!");
				System.err.println("a loop header's BytecodeOffsetTag is missing!");
			} else {
				int bci = bt.getBytecodeOffset();
				// dirty hack alert!
				// if(bci != 0) {
				loopHeaders.add(bci);
				loopFixPointIndicator.put(bci, 0);
				//System.out.println(bci + " is a loop header!");
				// }
			}

		}
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

		// this.isInvoke = true;
		// this.immediatePrevContextAnalysed = true;
		CallSite<M, N, A> callSite = new CallSite<M, N, A>(callerContext, callNode);

		// Check if the called method has a context associated with this entry flow:
		Context<M, N, A> calleeContext = getContext(method, entryValue);
		// If not, then set 'calleeContext' to a new context with the given entry flow.
		if (calleeContext == null) {
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
			this.immediatePrevContextAnalysed = false;
			//System.out.println("processCall returning null " + callerContext + " and " + method);
			return null;
		}
	}

	protected abstract A flowFunction(Context<M, N, A> context, N unit, A in);

}
