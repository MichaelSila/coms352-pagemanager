import java.util.LinkedList;
/**
 * 
 * @author michael
 * Class that will manage the memory frames.
 */
public class MemoryManger implements Runnable{

	/*
	 * Using Static Variables. I know thisra isn't the best practice,
	 * but it makes everything easier and I need all the help I can get.
	 * plus I'd use shared memory if I were doing this in C
	 */
	// the number of threads that have finished. Exit when this equals finalSize
	public static Integer finishedQueue;
	//Frame Table where the index corresponds to frame
	public static MemoryItem[] frameTable;
	//Final Size of the Finished Queue
	public static Integer finalSize;
	//Queue of Items to be referenced 
	public static LinkedList<MemoryItem> memoryQueue;
	//Page Size
	public static Integer pageSize;
	//Max Pages Per Process
	public static Integer maxPages;
	@Override
	public void run() {
		/*this is basically gonna run in the background for as long as there
		* are threads still reading from their files
		*/
		while (finishedQueue < finalSize) {
			if (!memoryQueue.isEmpty()) {
				//pop Item on Queue
				MemoryItem neededAddr=memoryQueue.removeFirst();
				boolean found=false;
				//Not going for best complexity 
				for(int i=0; i<frameTable.length;i++) {
					if (frameTable[i]!=null) { //nesting if statements for readability
						if (frameTable[i].getProcessNum().equals(neededAddr.getProcessNum()) && frameTable[i].getPageNum().equals(neededAddr.getPageNum()) ){
							frameTable[i]=neededAddr; //Might as well swap them
							found=true;
							System.out.println(neededAddr.getProcessNum()+
									" access address "
									+neededAddr.gettAddr()+
									"(page number = "+
										neededAddr.getPageNum()+
										", page offset = "+neededAddr.getOffset()+
										") in main memory (frame number =  "+
										i+
										").");
						}
						
					}
				}
				if (!found) {
					if (neededAddr.getPageNum()>=maxPages || neededAddr.getOffset() >= pageSize) {
						System.out.println("Invalid Address for Process "+neededAddr.getProcessNum());
					}
					else {
						System.out.println(neededAddr.getProcessNum()+
								" access address "
								+neededAddr.gettAddr()+
								"(page number = "+
									neededAddr.getPageNum()+
									", page offset = "+neededAddr.getOffset()+
									") not in main memory");
						synchronized (PageFaultHandler.QueueLock) {
							PageFaultHandler.faultQueue.add(neededAddr);
						}
					}
				}
			}
			PageFaultHandler.finished=true;
		}
		
	}
	/*
	 * Adds an Item to the Queue of items 
	 */
	public static synchronized void addToQueue(MemoryItem m) {
		memoryQueue.add(m);
	}
	
	public static synchronized void messageFinished() {
		finishedQueue++;
	}

}
