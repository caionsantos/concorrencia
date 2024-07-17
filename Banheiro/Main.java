import java.util.concurrent.Semaphore;

class Banheiro {
    private final int capacidadeMaxima = 3;
    private int pessoasNoBanheiro = 0;
    private char generoAtual = 'N';

    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore capacidade = new Semaphore(capacidadeMaxima);

    public void entrarBanheiro(int id, char genero) throws InterruptedException {
        while (true) {
            mutex.acquire();
            try {
                if (pessoasNoBanheiro == 0 || generoAtual == genero) {
                    if (capacidade.tryAcquire()) {
                        generoAtual = genero;
                        pessoasNoBanheiro++;
                        System.out.println("Pessoa " + id + " " + genero + " entrou no banheiro. " + pessoasNoBanheiro + " pessoas no banheiro.");
                        break;
                    }
                }
            } finally {
                mutex.release();
            }
            Thread.sleep(10);
        }
    }

    public void sairBanheiro(int id, char genero) throws InterruptedException {
        mutex.acquire();
        try {
            pessoasNoBanheiro--;
            System.out.println("Pessoa " + id + " " + genero + " saiu do banheiro. " + pessoasNoBanheiro + " pessoas no banheiro.");
            if (pessoasNoBanheiro == 0) {
                generoAtual = 'N';
            }
            capacidade.release();
        } finally {
            mutex.release();
        }
    }
}

class Pessoa implements Runnable {
    private final Banheiro banheiro;
    private final int id;
    private final char genero;

    public Pessoa(Banheiro banheiro, int id, char genero) {
        this.banheiro = banheiro;
        this.id = id;
        this.genero = genero;
    }

    @Override
    public void run() {
        System.out.println("Pessoa " + id + " " + genero + " entrou na fila.");
        try {
            banheiro.entrarBanheiro(id, genero);
            Thread.sleep((long) (Math.random() * 1000));
            banheiro.sairBanheiro(id, genero);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Banheiro banheiro = new Banheiro();

        for (int i = 0; i < 100; i++) {
            char genero = (Math.random() < 0.5) ? 'H' : 'M';
            Pessoa pessoa = new Pessoa(banheiro, i, genero);
            Thread pessoaThread = new Thread(pessoa);
            pessoaThread.start();
        }
    }
}
