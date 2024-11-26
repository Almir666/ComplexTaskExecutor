import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ComplexTaskExecutor {
    private final CyclicBarrier cyclicBarrier;
    private final ExecutorService executorService;
    private final int[] result;
    private int numberOfTasks;

    public ComplexTaskExecutor(int numberOfTasks) {

        this.cyclicBarrier = new CyclicBarrier(numberOfTasks);
        this.executorService = Executors.newFixedThreadPool(numberOfTasks);
        this.result = new int[numberOfTasks];
        this.numberOfTasks = numberOfTasks;
    }

    public void executeTasks() {
        for (int i = 1; i < numberOfTasks; i++) {
            final int id = i;
            executorService.submit(() -> {
                try {
                    ComplexTask task = new ComplexTask(id);
                    result[id] = task.execute();
                    cyclicBarrier.await();
                    combineResult();
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void combineResult() {
        int total = 0;
        for (int el : result) {
            total += el;
        }
        System.out.println("Combined result: " + total);
    }

    public static void main(String[] args) {
        ComplexTaskExecutor taskExecutor = new ComplexTaskExecutor(5);
        Runnable testRunnable = () -> {
            System.out.println(Thread.currentThread().getName() + " started the test.");

            taskExecutor.executeTasks();

            System.out.println(Thread.currentThread().getName() + " completed the test.");
        };

        Thread thread1 = new Thread(testRunnable, "TestThread-1");
        Thread thread2 = new Thread(testRunnable, "TestThread-2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    class ComplexTask {
        private final int taskId;

        public ComplexTask(int taskId) {
            this.taskId = taskId;
        }

        public int execute() {
            System.out.println("Task: " + taskId + " is being executed by " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return taskId * 10;
        }
    }
}
