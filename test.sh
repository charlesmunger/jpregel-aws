mvn deploy
java -server -verbosegc -Xms2g -Xmx2g -XX:+UseConcMarkSweepGC \
     -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:+UseCompressedStrings \
          -cp target/jpregel.jar clients.SsspBinaryTree \
                examples/SsspBinaryTree 1
