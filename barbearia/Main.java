import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Barbearia {
    private final int cadeiras;
    private final Semaphore semCadeiras;
    private final Semaphore semBarbeiro;
    private final Lock lock = new ReentrantLock(true);

    public Barbearia(int cadeiras) {
        this.cadeiras = cadeiras;
        this.semCadeiras = new Semaphore(cadeiras);
        this.semBarbeiro = new Semaphore(0);
    }

    public void entraCliente(int clienteId) {
        lock.lock();
        try {
            if (semCadeiras.tryAcquire()) {
                System.out.println("Cliente " + clienteId + " sentou-se na sala de espera.");
                semBarbeiro.release();
                semCadeiras.acquire();
            } else {
                System.out.println("Cliente " + clienteId + " foi embora, não há cadeiras disponíveis.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void atendeCliente() {
        while (true) {
            try {
                semBarbeiro.acquire();
                lock.lock();
                try {
                    System.out.println("Barbeiro está atendendo um cliente.");
                    Thread.sleep(2000);
                    System.out.println("Barbeiro terminou de atender um cliente.");
                    semCadeiras.release();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Cliente implements Runnable {
    private final Barbearia barbearia;
    private final int clienteId;

    public Cliente(Barbearia barbearia, int clienteId) {
        this.barbearia = barbearia;
        this.clienteId = clienteId;
    }

    @Override
    public void run() {
        System.out.println("Cliente " + clienteId + " chegou.");
        barbearia.entraCliente(clienteId);
    }
}

public class Main {
    public static void main(String[] args) {
        Barbearia barbearia = new Barbearia(5);

        Thread barbeiro = new Thread(barbearia::atendeCliente);
        barbeiro.start();

        for (int i = 1; i <= 100; i++) {
            Cliente cliente = new Cliente(barbearia, i);
            Thread clienteThread = new Thread(cliente);
            clienteThread.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
