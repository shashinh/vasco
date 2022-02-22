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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.spark.solver.PropWorklist;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.reflection.ReflInliner;
import soot.jimple.toolkits.reflection.ReflectiveCallsInliner;
import soot.rtlib.tamiflex.DefaultHandler;
import soot.rtlib.tamiflex.IUnexpectedReflectiveCallHandler;
import soot.rtlib.tamiflex.OpaquePredicate;
import soot.rtlib.tamiflex.ReflectiveCalls;
import soot.rtlib.tamiflex.SootSig;
import soot.rtlib.tamiflex.UnexpectedReflectiveCall;
import vasco.CallSite;
import vasco.Context;
import vasco.ContextTransitionTable;

/**
 * A main class for testing call graph construction using a Flow and Context
 * Sensitive Points-to Analysis (FCPA).
 * 
 * <p>Usage: <tt>java vasco.callgraph.CallGraphTest [-cp CLASSPATH] [-out DIR] [-k DEPTH] MAIN_CLASS</tt></p>
 * 
 * @author Rohan Padhye
 */
public class CallGraphTest {
	
	private static String outputDirectory;

	public static void main(String args[]) {
		outputDirectory = ".";
		String classPath = System.getProperty("java.class.path");
		String mainClass = null;
		int callChainDepth = 10;
		String reflectionLog = null;

		/* ------------------- OPTIONS ---------------------- */
		try {
			int i=0;
			while(true){
				if (args[i].equals("-cp")) {
					classPath = args[i+1];
					i += 2;
				} else if (args[i].equals("-out")) {
					outputDirectory = args[i+1];
					File outDirFile = new File(outputDirectory);
					if (outDirFile.exists() == false && outDirFile.mkdirs() == false) {
						throw new IOException("Could not make output directory: " + outputDirectory);
					}
					i += 2;
				} else if (args[i].equals("-k")) { 
					callChainDepth = Integer.parseInt(args[i+1]);
					i += 2;
				} else if (args[i].equals("-reflog")) {
					reflectionLog = args[i+1];
					i += 2;
				} else {
					mainClass = args[i];
					i++;
					break;
				}
			}
			if (i != args.length || mainClass == null)
				throw new Exception();
		} catch (Exception e) {
			System.out.println("Usage: java vasco.callgraph.CallGraphTest [-cp CLASSPATH] [-out DIR] [-k DEPTH] MAIN_CLASS");
			System.exit(1);
		}

		/* ------------------- SOOT OPTIONS ---------------------- */
		String[] sootArgs = {
				"-cp", classPath, "-pp", 
				//"-src-prec", "J",
				"-w", "-app", 
//				"-x", "soot.*",
//				"-x", "java.*",
//				"-x", "jdk.*",
				//"-no-bodies-for-excluded",
				"-keep-line-number",
				"-keep-bytecode-offset",
				"-p", "cg", "implicit-entry:false",
				"-p", "cg.spark", "enabled",
				"-p", "cg.spark", "simulate-natives",
				//"-p", "cg", "safe-forname",
				"-p", "cg", "safe-newinstance",
				//"-p", "cg", "reflection-log:" + reflectionLog,
				"-include", "org.apache.",
				"-include", "org.w3c.",
				 "-allow-phantom-refs",
				"-main-class", mainClass,
				"-f", "J", 
				"-d", "sootOutput", mainClass
		};
		
//		String[] sootArgs = {
//				"-cp", classPath, "-pp",
//				"-w", "-app", 
//				"-src-prec", "J",
//				"-keep-line-number",
//				"-keep-bytecode-offset",
//				"-p", "cg", "implicit-entry:false",
//				"-p", "cg.spark", "enabled",
//				"-p", "cg.spark", "simulate-natives",
//				"-p", "cg", "safe-forname",
//				"-p", "cg", "safe-newinstance",
//				"-p", "cg", "reflection-log:" + reflectionLog,
//				"-include", "org.apache.",
//				"-include", "org.w3c.",
//				"-main-class", mainClass,
//				"-f", "c", 
//				"-d", "sootOutput", mainClass
//		};
		
//		-cp test-classes/test44/jimple -out test-classes/test44/results -reflog test-classes/test44/out/refl.log Main
		//-cp .:test-classes/test44 -out test-classes/test44/results -reflog test-classes/test44/out/refl.log Main
		//-cp test-classes/test45/slf4j.jar:test-classes/test45/logback.jar:test-classes/test45 -out test-classes/test45/results -reflog test-classes/test45/refl.log Main
		
//		String[] sootArgs = {
//				"-cp", classPath+":/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar:out", "-pp", 
//				"-w", "-app",
//				"-whole-program",
//				"-keep-line-number", "-keep-offset",
//				"-p", "cg", "implicit-entry:false",
//				"-p", "cg.spark", "enabled",
//				"-p", "cg.spark", "simulate-natives",
//				"-p", "cg", "safe-forname",
//				"-p", "cg", "safe-newinstance",
//				"-p", "cg", "reflection-log:out/refl.log",
//				"-d" , "sootified/all",
//				"-main-class", mainClass,
//				"-f", "none", mainClass 
//		};
		
//		String[] sootArgs = {
//				"-cp", classPath+":/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar:out", "-pp", 
//				"-w", "-app",
//				"-keep-line-number", "-keep-offset",
//				"-p", "cg", "implicit-entry:false",
//				"-p", "cg", "reflection-log:out/refl.log",
//				"-p", "cg.spark", "enabled",
//				"-include", "org.apache.",
//				"-include", "org.w3c.",
//				"-main-class", mainClass,
//				"-f", "none", mainClass 
//		};
		
		System.out.println("Soot args are: " + Arrays.toString(sootArgs));

		/* ------------------- ANALYSIS ---------------------- */
		CallGraphTransformer cgt = new CallGraphTransformer();
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.fcpa", cgt));
		
	    /*PackManager.v().getPack("wjpp").add(new Transform("wjpp.inlineReflCalls", new ReflectiveCallsInliner()));
	    final Scene scene = Scene.v();
	    scene.addBasicClass(Object.class.getName());
	    scene.addBasicClass(SootSig.class.getName(), SootClass.BODIES);
	    scene.addBasicClass(UnexpectedReflectiveCall.class.getName(), SootClass.BODIES);
	    scene.addBasicClass(IUnexpectedReflectiveCallHandler.class.getName(), SootClass.BODIES);
	    scene.addBasicClass(DefaultHandler.class.getName(), SootClass.BODIES);
	    scene.addBasicClass(OpaquePredicate.class.getName(), SootClass.BODIES);
	    scene.addBasicClass(ReflectiveCalls.class.getName(), SootClass.BODIES);*/
	    
//		Scene.v().addBasicClass("avrora.Main", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("java.security.MessageDigestSpi", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("org.dacapo.harness.Avrora", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("sun.net.www.protocol.jar.Handler", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("sun.security.provider.SHA", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("avrora.Defaults$AutoProgramReader", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("avrora.actions.SimAction", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("avrora.monitors.LEDMonitor", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("avrora.monitors.PacketMonitor", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("avrora.sim.platform.Mica2$Factory", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("avrora.sim.types.SensorSimulation", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("sun.net.www.protocol.jar.Handler", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("sun.security.provider.Sun", SootClass.SIGNATURES);
//		Scene.v().addBasicClass("org.apache.commons.cli.HelpFormatter", SootClass.SIGNATURES);
		
		//ReflInliner.main(sootArgs);
		
		soot.Main.main(sootArgs);
		
		PointsToAnalysis pointsToAnalysis = cgt.getPointsToAnalysis();
		

		
		/* ------------------- LOGGING ---------------------- */
		try {
			//printCallSiteStats(pointsToAnalysis);
			//printMethodStats(pointsToAnalysis);
			//dumpCallChainStats(pointsToAnalysis, callChainDepth);
			printLoopInvariantPTGS(pointsToAnalysis);
			printCallsiteInvariants(pointsToAnalysis);
		} catch (FileNotFoundException e1) {
			System.err.println("Oops! Could not create log file: " + e1.getMessage());
			System.exit(1);
		}
		
	}
	
	
	public static List<SootMethod> getSparkExplicitEdges(Unit callStmt) {
		Iterator<Edge> edges = Scene.v().getCallGraph().edgesOutOf(callStmt);
		List<SootMethod> targets = new LinkedList<SootMethod>();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			if (edge.isExplicit()) {
				targets.add(edge.tgt());
			}
		}
		return targets;
	}
	
	public static List<SootMethod> getSparkExplicitEdges(SootMethod sootMethod) {
		Iterator<Edge> edges = Scene.v().getCallGraph().edgesOutOf(sootMethod);
		List<SootMethod> targets = new LinkedList<SootMethod>();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			if (edge.isExplicit()) {
				targets.add(edge.tgt());
			}
		}
		return targets;
	}
	
	
	private static Set<SootMethod> dirtyMethods = new HashSet<SootMethod>();
	
	private static void markDirty(Unit defaultSite) {
		List<SootMethod> methods = getSparkExplicitEdges(defaultSite);
		while (methods.isEmpty() == false) {
			SootMethod method = methods.remove(0);
			if (dirtyMethods.contains(method)) {
				continue;
			} else {
				dirtyMethods.add(method);
				methods.addAll(getSparkExplicitEdges(method));
			}
		}
	}
	
	public static void printLoopInvariantPTGS(PointsToAnalysis pta) throws FileNotFoundException {
		
		for(String methodName : pta.loopInvariants.keySet()) {

			PrintWriter pw = new PrintWriter(outputDirectory + "/loop-invariants-" + methodName + ".txt");
			Map<Integer, PointsToGraph> map = pta.loopInvariants.get(methodName);
			List<String> sList = new ArrayList<String>();
			for(Integer i : map.keySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(i + ":");
				sb.append(map.get(i).prettyPrintInvariant(pta));
				sList.add(sb.toString());
			}
			
			pw.print(String.join(";", sList));
			pw.close();
		}
	}
	
	public static void printCallsiteInvariants(PointsToAnalysis pta) throws FileNotFoundException {
		Map<String, Map<Integer, Set<String>>> callSiteInvariants = pta.callSiteInvariants;
		
		PrintWriter p = new PrintWriter(outputDirectory + "/caller-indices" + ".txt");
		Map<String, Integer> calleeIndexMap = pta.calleeIndexMap;
		Map<Integer, String> calleeIndexMapSorted = new HashMap<Integer, String>();
		for(String key : calleeIndexMap.keySet()) {
			calleeIndexMapSorted.put(calleeIndexMap.get(key), key);
		}
		for(Integer key : calleeIndexMapSorted.keySet()) {
			p.println(calleeIndexMapSorted.get(key));
		}
		p.close();
		
		for(String methodName :callSiteInvariants.keySet()) {
			boolean hasNull = false;
			boolean hasConst = false;
			boolean hasString = false;
			boolean hasGlobal = false;
			PrintWriter pw = new PrintWriter(outputDirectory + "/callsite-invariant-" + methodName + ".txt");
			Map<Integer, Set<String>> map = callSiteInvariants.get(methodName);

			List<String> sList = new ArrayList<String>();
			for(Integer m : map.keySet()) {
				StringBuilder sb = new StringBuilder();
				//sb.append(m + ":");
				//sb.append("(");
				Set<String> argsList = map.get(m);
				//do the aggregation here, argsList is of the form 
				//[Main.main-3, A.foo-3, Main.main-19, Main.main-9, Main.main-27]
				Map<String, String> calleeMap = new HashMap<String, String>();
				Set<String> set = new HashSet<String>();
				for(String s : argsList) {
					if(s.equals("n"))
						hasNull = true;
					else if (s.equals("s")) 
						hasString = true;
					else if (s.equals("c")) 
						hasConst = true;
					else if (s.equals("g")) 
						hasGlobal = true;
					else {
						String [] temp = s.split("-");
						String caller = temp[0];
						String bci = temp[1];
						if(calleeMap.containsKey(caller)) {
							String val = calleeMap.get(caller);
							val += "." + bci;
							calleeMap.replace(caller, val);
						} else {
							calleeMap.put(caller, bci);
						}
					}
				}

				
				for(String k : calleeMap.keySet()) {
					set.add(k + "-" + calleeMap.get(k));
				}
				
				if(hasNull) 
					set.add("n");
				if(hasConst) 
					set.add("c");
				if(hasString) 
					set.add("s");
				if(hasGlobal)
					set.add("g");
				
				//System.out.println(set);
				sb.append(String.join(" ", set));
				//sb.append(String.join(" ", argsList));
				
				
				
//				for(int i = 0; i < argsMap.size(); i ++) {
//					sb.append(i + ":");
//					sb.append(String.join(" ", argsMa`argsMap.get(i)));
//					if(i + 1 <argsMap.size())
//						sb.append(",");
//				}
				//sb.append(")");
				if(sb.length() != 0)
					sList.add(sb.toString());
			}
			
			pw.print(String.join("\n", sList));
			pw.close();
		}
		

	}
	
	
	public static void printCallSiteStats(PointsToAnalysis pta) throws FileNotFoundException {
		// Get context-transition table
		ContextTransitionTable<SootMethod,Unit,PointsToGraph> ctt = pta.getContextTransitionTable();
		Map<Context<SootMethod,Unit,PointsToGraph>,Set<CallSite<SootMethod,Unit,PointsToGraph>>> callSitesWithinContexts = ctt.getCallSitesOfContexts();
		Map<CallSite<SootMethod,Unit,PointsToGraph>,Map<SootMethod,Context<SootMethod,Unit,PointsToGraph>>> transitions = ctt.getTransitions();
		Set<CallSite<SootMethod,Unit,PointsToGraph>> defaultCallSites = ctt.getDefaultCallSites();
		
		// Initialise output stream
		PrintWriter csv = new PrintWriter(outputDirectory + "/sites.csv");
		csv.println("FcpaEdges, SparkEdges, Context, CallSite");
		
		// The visited set
		Set<Context<SootMethod,Unit,PointsToGraph>> visited = new HashSet<Context<SootMethod,Unit,PointsToGraph>>();
		
		// Maintain a stack of contexts to process
		Stack<Context<SootMethod,Unit,PointsToGraph>> stack = new Stack<Context<SootMethod,Unit,PointsToGraph>>();
		
		// Initialise it with the main context
		Context<SootMethod,Unit,PointsToGraph> source = pta.getContexts(Scene.v().getMainMethod()).get(0);
		stack.push(source);

		// Now recursively (using stacks) mark reachable contexts
		while (stack.isEmpty() == false) {
			// Get the next item to process
			source = stack.pop();
			// Add successors
			if (callSitesWithinContexts.containsKey(source)) {
				// The above check is there because methods with no calls have no entry
				for (CallSite<SootMethod,Unit,PointsToGraph> callSite : callSitesWithinContexts.get(source)) {
					// My edges are -1 for "default" sites, and whatever the CTT has otherwise
					int myEdges = defaultCallSites.contains(callSite) ? -1 : transitions.get(callSite).size();
					// Get SPARK's edges from the Soot call graph
					int sparkEdges = getSparkExplicitEdges(callSite.getCallNode()).size();
					
					// Log this
					csv.println(myEdges + ", " + sparkEdges + ", " + source + ", " +
							"\"" + callSite.getCallNode() + "\"");

					if (myEdges > 0) {
						for (SootMethod method : transitions.get(callSite).keySet()) {
							Context<SootMethod,Unit,PointsToGraph> target = transitions.get(callSite).get(method);
							// Don't process the same element twice
							if (visited.contains(target) == false) {
								// Mark reachable
								visited.add(target);
								// Add it's successors also later
								stack.push(target);
							}
						}
					} else if (myEdges == -1) {
						// Default call-site, so mark reachable closure as "dirty"
						markDirty(callSite.getCallNode());
					}
				}
			}
			
		}
		// Close the CSV file
		csv.close();		
	}
	
	public static void printMethodStats(PointsToAnalysis pta) throws FileNotFoundException {
		// Initialise output stream
		PrintWriter csv = new PrintWriter(outputDirectory + "/methods.csv");
		csv.println("Method, Contexts, Application?, Dirty?");
		for (SootMethod method : pta.getMethods()) {
			csv.println("\"" + method + "\"" + ", " + pta.getContexts(method).size() +
					", " + (method.getDeclaringClass().isApplicationClass() ? 1 : 0) +
					", " + (dirtyMethods.contains(method) ? 1 : 0));
		}
		
		// Close the CSV file
		csv.close();
	}
	
	public static void dumpCallChainStats(PointsToAnalysis pta, int maxDepth) throws FileNotFoundException {		
		// Initialise output stream
		PrintWriter txt = new PrintWriter(new FileOutputStream(outputDirectory + "/chains.txt"), true);
		Context<SootMethod,Unit,?> mainContext = pta.getContexts(Scene.v().getMainMethod()).get(0);
		SootMethod mainMethod = Scene.v().getMainMethod();
		
		txt.println("FCPA Chains");
		txt.println("------------");
		for(int k=1; k<=maxDepth; k++) {
			txt.println("k=" + k + ": " + countCallChains(pta, mainContext, k));
		}
		txt.println("Spark Chains");
		txt.println("------------");
		for(int k=1; k<=maxDepth; k++) {
			txt.println("k=" + k + ": " + countCallChains(mainMethod, k));
		}
		
		txt.close();	
		
	}
	
	private static long countCallChains(SootMethod method, int k) {
		if (k == 0)
			return 1;
		
		long count = 1;
		Iterator<Edge> edges = Scene.v().getCallGraph().edgesOutOf(method);
		while(edges.hasNext()) {
			Edge edge = edges.next();
			if (edge.isExplicit()) {
				SootMethod target = edge.tgt();
				count = count + countCallChains(target, k-1);
			}
		}
		return count;
	}
	
	private static long countCallChains(PointsToAnalysis pta, Context<SootMethod,Unit,?> context, int k) {		
		if (k == 0)
			return 1;
		
		long count = 1;
		ContextTransitionTable<SootMethod,Unit,?> ctt = pta.getContextTransitionTable();
		if (ctt.getCallSitesOfContexts().containsKey(context)) {
			for (CallSite<SootMethod,Unit,?> callSite : ctt.getCallSitesOfContexts().get(context)) {
				if (ctt.getDefaultCallSites().contains(callSite)) {
					Iterator<Edge> edges = Scene.v().getCallGraph().edgesOutOf(callSite.getCallNode());
					while(edges.hasNext()) {
						SootMethod target = edges.next().tgt();
						count = count + countCallChains(target, k-1);
					}
				} else if (ctt.getTransitions().containsKey(callSite) && ctt.getTransitions().get(callSite) instanceof Map) {
					for (Context<SootMethod,Unit,?> target : ctt.getTransitions().get(callSite).values()) {
						if (target.getMethod().getName().equals("<clinit>")) {
							continue;
						} else {
							count = count + countCallChains(pta, target, k-1);
						}
					}
				}
			}
		}
		
		return count;
		
	}

	@Test
	public void testCallGraphAnalaysis() {
		// TODO: Compare output with an ideal (expected) output
		CallGraphTest.main(new String[]{"-k", "-3", "-out", "target/test-results/CallGraphTestResults", "vasco.tests.CallGraphTestCase"});
	}
	
}
