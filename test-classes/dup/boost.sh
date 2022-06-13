#!/bin/bash

boost () {
	rm -r out/ boosted/
	java -javaagent:/home/shashin/projects/tamiflex-jars/poa-2.0.3.jar $1
	java -jar /home/shashin/projects/tamiflex-jars/booster.jar -pp -w -p cg reflection-log:out/refl.log  -keep-line-number -keep-bytecode-offset -p jb preserve-source-annotations:true -p jb.ulp enabled:false -p jb.dae enabled:false -p jb.cp-ule enabled:false -p jb.cp enabled:false -p jb.lp enabled:false -p jb.dtr enabled:false -p jb.ese enabled:false -p jb.a enabled:false -p jb.ule enabled:false -p jb.ne enabled:false -p jb.uce enabled:false -p jb.tt enabled:false -p jop.cp enabled:false -p jop.dae enabled:false -p jop.uce1 enabled:false -p jop.ule enabled:false -p jop enabled:false -verbose -debug -dump-body jb -cp out/ -d boosted -main-class $1 $1
}
