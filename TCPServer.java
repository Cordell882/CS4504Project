   import java.io.*;
   import java.net.*;
   import java.util.Arrays;

    public class TCPServer {
       public static void main(String[] args) throws IOException {
      	
			// Variables for setting up connection and communication
         Socket Socket = null; // socket to connect with ServerRouter
         PrintWriter out = null; // for writing to ServerRouter
         BufferedReader in = null; // for reading form ServerRouter
			InetAddress addr = InetAddress.getLocalHost();
			String host = addr.getHostAddress(); // Server machine's IP			
			String routerName = "172.20.10.2"; // ServerRouter host name
			int SockNum = 5555; // port number
			
			// Tries to connect to the ServerRouter
         try {
            Socket = new Socket(routerName, SockNum);
            out = new PrintWriter(Socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
         } 
             catch (UnknownHostException e) {
               System.err.println("Don't know about router: " + routerName);
               System.exit(1);
            } 
             catch (IOException e) {
               System.err.println("Couldn't get I/O for the connection to: " + routerName);
               System.exit(1);
            }
				
      	// Variables for message passing			
         String fromServer; // messages sent to ServerRouter
         String fromClient; // messages received from ServerRouter      
 			String address ="172.20.10.5"; // destination IP (Client)
         
         String[] str;
         int[] arr = {0};
         boolean alr = false;
			
			// Communication process (initial sends/receives)
			out.println(address);// initial send (IP of the destination Client)
			fromClient = in.readLine();// initial receive from router (verification of connection)
			System.out.println("ServerRouter: " + fromClient);
			         
			// Communication while loop
      	while ((fromClient = in.readLine()) != null) {
            System.out.println("Client said: " + fromClient);
            if (fromClient.equals("Bye.")) // exit statement
					break;
				if(fromClient.contains("."))
            {
               fromServer = fromClient.toUpperCase(); // converting received message to upper case
               out.println(fromServer);
            }
            else if(fromClient.contains("!"))
            {
               if(!alr == true)
               {
                  System.out.println("End of read file I would run sort here");
                  
                  arr = runSort(arr, 2);
                  System.out.println("Array sorted: " + Arrays.toString(arr));
                  out.println("Array sorted: " + Arrays.toString(arr));
               }
               alr = true;
            }
            else
            {   
            
               fromServer = fromClient.toUpperCase(); // converting received message to upper case
               
               str = fromServer.replaceAll("\\[", "").replaceAll("]", "").split(" ");
               
               System.out.println("String before: ");
               for(int i = 0; i < str.length; i++)
               {
                  System.out.print(str[i] + " ");
               }
               System.out.println();
               
               arr = new int[str.length];
               
               for(int i = 0; i < str.length-1; i++)
               {
                  arr[i] = Integer.valueOf(str[i]);
               }
               
               System.out.println("Array: ");
               for(int i = 0; i < arr.length; i++)
               {
                  System.out.print(arr[i] + " ");
               }
               
               System.out.println();
   				System.out.println("Server said: " + fromServer);
               out.println(fromServer); // sending the converted message back to the Client via ServerRouter
               
            }
         }
			
			// closing connections
         out.close();
         in.close();
         Socket.close();
      }
      //The merge and mergesort algorithm
      public static void mergeSort(int[] arr, int left, int right)
      {

         if(left < right)
         {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid+1, right);
            merge(arr, left, mid, right);
         }
      }
   
      public static void merge(int[] arr, int left, int mid, int right)
      {
         int[] arr2 = new int[right - left + 1];
         int l = left;
         int m = mid + 1;
         int k = 0;
         
         while(l <= mid && m <= right)
         {
            if(arr[l] <= arr[m])
            {
               arr2[k++] = arr[l++];
            }
            else
            {
               arr2[k++] = arr[m++];
            }
         } 
      
         while(l <= mid)
         {
            arr2[k++] = arr[l++];
         }
      
         while(m <= right)
         {
            arr2[k++] = arr[m++];
         }
      
         System.arraycopy(arr2, 0, arr, left, arr2.length);
      }
   
   
      //Driver method for creating threads, dividing work, and starting
      public static int[] runSort(int[] arr, int threadNum)
      {
         long tTime = System.nanoTime();

         //Create threads and divide array by number of threads to segment data
         Thread[] threads = new Thread[threadNum];
         int segSize = arr.length / threadNum; 
      
         //go through all threads and give them their sections to sort and start threads
         for(int i = 0; i < threadNum; i++)
         {
            int sIn = i * segSize;
            int eIn = (i == threadNum - 1) ? arr.length - 1 : (sIn + segSize - 1);
         
            threads[i] = new Thread(new threadJob(arr, sIn, eIn));
            //System.out.print("Thread: " + i + " will be sorting array segment: ");
            //printArr(arr, sIn, eIn);
            threads[i].start();
         
         }
      
         //have all threads wait for completion of eachother to final merge. 
         for(Thread thread: threads)
         {
            try
            {
               thread.join();
            }
            catch(InterruptedException e)
            {
               e.printStackTrace();
            }
         }
      
         //now that segments of the array has been sorted go through and merge the segments into 1
         //This is a simple implementation and will just be merging the first section with each of the next sections
         //That is the first merge will merge section 1 and 2 then the second merge will merge section 1+2 with section 3 and so on
         for(int i = 0; i < threadNum; i++)
         {
            int sIn = i * segSize;
            int mid = i*segSize == 0? 0 : i*segSize-1;
            int eIn = (i == threadNum - 1) ? arr.length - 1 : (sIn + segSize - 1);
         
            //System.out.println("Merging values: 0, " + mid + ", " + eIn); 
            merge(arr, 0, mid, eIn);
         }  
         return arr;
      }
   
      /*
      this method was just for testing purposes and can be deleted 
      public static void printArr(int[] arr, int sIn, int eIn)
      {
         for(int i = sIn; i <= eIn; i++)
         {
            System.out.print(arr[i] + ", ");
         }
         System.out.println();
      }
      */
   
      //The class created that implements runnable to tell the threads what to do
      static class threadJob implements Runnable
      {
         private int[] arr;
         private int sIn;
         private int eIn;
      
         public threadJob(int[] arr, int sIn, int eIn)
         {
            this.arr = arr;
            this.sIn = sIn;
            this.eIn = eIn;
         }
      
         @Override
         public void run()
         {
            mergeSort(arr, sIn, eIn);
         }
      }
   }
