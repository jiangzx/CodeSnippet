##### how to use ParallelProcessor class to handle parallel task.
```java
 // user defined self Operation: find someone file from DIR path
 	class FindFileByExtenstion extends Operation<String>{
 		// This attribute will store the full path of the folder this task is going to process.
 		public final String[] dir={"/Users/test/Documents/logs1","/Users/test/Documents/logs","/Users/test/Documents/doc"};
 		//save find path
 		List<String> path = new ArrayList<String>();
 		//file extension 
 		private String extension="log";
 		
 		void doIt(String dir) {
 			File files[] = new File(dir).listFiles();
 			for(File file : files){
 				if(file.isFile() && file.getName().endsWith(extension)){
 					path.add(file.getAbsolutePath());
					System.out.println("result:"+file.getName());
 				}
 				
 			}
 		}
 	}}

// how to using it?
  public void test() {
   	FindFileByExtenstion op = new FindFileByExtenstion();
 			ParallelProcessor<String> pr = new ParallelProcessor<String>(op.dir,op, 0,op.dir.length);
 			ForkJoinPool pool = new ForkJoinPool();
 			pool.invoke(pr);
 			pool.shutdown();
 			try {
 				pool.awaitTermination(1, TimeUnit.DAYS);
 			} catch (InterruptedException e1) {
 			}
 			System.out.println("result:"+op.path);
 	 }}
 	 
```

##### more example please refer to :
##### src/test/java/com/webex/cloudservice/common/threads/fj/TestParallelProcessor.java
##### src/test/java/com/webex/cloudservice/common/threads/fj/TestParallelProcessorWithResult.java