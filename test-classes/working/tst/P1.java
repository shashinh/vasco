class P1{
    public static void main(String []args){
    try {
        T t1;
        T t2;
        Buf l;
        int a;
        l = new Buf();
        t1 = new T();
        t2 = new T();
        t1.set(l);
        t2.set(l);

        t1.run();
        t2.run();

        a = 10;
        a = l.f1(a);

        t1.join();
        t2.join();
        }catch (Exception e){}

    }

    
}

class Buf{
    public int f1(int a){
        a = a +20;
        return a;
    }
}

class A{
    public Buf ret(){
        Buf c;
        c = new Buf();
        return c;
    }
}

class T extends Thread{
    Buf b;

    public void set(Buf k){
        b = k;
    }


    public void f2(int a){
        try{
            synchronized(b){
                a = 20; 
            }
        }catch (Exception e){}
    }

    public void run(){
        int a;

        a =10;

        f2(a);

    }
}