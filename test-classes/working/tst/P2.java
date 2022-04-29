class P2 {
    public static void main(String []args){
        T1 a;
        T1 b;
        a = new T1();
        b = new T1();
        a.run();
        b.run();
        try{
            a.join();
            b.join();
        }catch (Exception e){}
    }
}


class T1 extends Thread{
    public void newExec(int a){
    try{
        T2 ot;
        ot = new T2();
        ot.set(a);
        ot.run();
        ot.join();
    }catch (Exception e){}
    }

    public void run(){
        int a;
        a = 7;
        newExec(a);
    }
}


class T2 extends Thread{
    int b;
    public void set(int k){
        b = k;
    }

    public int f1(int a){
        return a + 2;
    }
    public void run(){
        b = f1(b);
    }
}