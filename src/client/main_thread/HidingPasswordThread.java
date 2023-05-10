package client.main_thread;

public class HidingPasswordThread extends Thread{
    private boolean stop;
    private boolean first;
        /**
         * Begin masking...display asterisks (*)
         */
        public void run () {
            stop = true;
            while (stop) {
                System.out.print("\010*");

                try {
                    Thread.currentThread().sleep(1);
                } catch(InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        /**
         * Instruct the thread to stop masking
         */
        public void stopMasking() {
            this.stop = false;
        }
    }