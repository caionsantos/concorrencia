import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

class Barbearia {
    private final int cadeiras;
    private final Semaphore semCadeiras;
    private final Semaphore semBarbeiro;
    private final Lock lock = new ReentrantLock(true);
    private boolean dormindo;

    public Barbearia(int cadeiras) {
        this.cadeiras = cadeiras;
        this.semCadeiras = new Semaphore(cadeiras);
        this.semBarbeiro = new Semaphore(1);
        this.dormindo = false;
    }

    public void entraCliente(int clienteId) throws InterruptedException {
        if (semCadeiras.availablePermits() == 10 && this.dormindo){
            lock.lock();
            this.dormindo = false;
            System.out.println("Barbeiro foi acordado por " + clienteId);
            lock.unlock();
            semCadeiras.acquire();
            this.atendeCliente(clienteId);
        }
        else if (semCadeiras.tryAcquire()) {
            System.out.println("Cliente " + clienteId + " sentou-se na sala de espera.");
            this.atendeCliente(clienteId);
        } else {
            System.out.println("Cliente " + clienteId + " foi embora, não há cadeiras disponíveis.");
        }
    }

    public void atendeCliente(int clienteId) throws InterruptedException {
        semCadeiras.release();
        semBarbeiro.acquire();
        System.out.println("Barbeiro está atendendo o cliente " + clienteId);
        Thread.sleep(2000);
        System.out.println("Barbeiro terminou de atender o cliente " + clienteId);

        this.dormindo = true;
        semBarbeiro.release();
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
        try {
            Thread.sleep(new Random().nextInt(100000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Cliente " + clienteId + " chegou.");
        try {
            barbearia.entraCliente(clienteId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Barbearia barbearia = new Barbearia(10);

        for (int i = 1; i < 100; i++) {
            Cliente cliente = new Cliente(barbearia, i);
            Thread clienteThread = new Thread(cliente);
            clienteThread.start();
        }
    }
}
