//this file is annotated with bcis and method indices, when boosted with the following command line args
//java -jar ../../../tamiflex-jars/booster.jar -cp out -pp -w -p cg reflection-log:out/refl.log -p jb preserve-source-annotations:true -p jb.ulp enabled:false -p jb.dae enabled:false -p jb.cp-ule enabled:false -p jb.cp enabled:false -p jb.lp enabled:false -p jb.dtr enabled:false -p jb.ese enabled:false -p jb.a enabled:false -p jb.ule enabled:false -p jb.ne enabled:false -p jb.ese enabled:false -p jb.tt enabled:false -p bb.lp enabled:false -p jop enabled:false -p bop enabled:false -java-version 1.8 -d boosted -keep-line-number -keep-bytecode-offset -main-class LinkedList LinkedList

import java.lang.reflect.*;

class LinkedList{
	static {
		try {
			Class clLL = Class.forName("LL");
			Class clEl = Class.forName("Element");
			Class clList = Class.forName("List");
		} catch (Exception ex) { }
	}

    public static void main(String[] a){ //mi 1
	//System.out.println(new LL().Start());
	//insert constructor code here
	try {
	Constructor cons = LL.class.getConstructor();
	LL ll = (LL) cons.newInstance();
	System.out.println(ll.Start());
	} catch (Exception ex) { System.out.println(ex); }
    }
}
class A {
	LinkedList abc;
	LinkedList abc4;
	LinkedList abc5;
}

class Element {
    int Age ;          
    int Salary ;
    boolean Married ;

    //Element.<init> mi 276
    //
    // Initialize some class variables
    public boolean Init(int v_Age, int v_Salary, boolean v_Married){ //mi 277
	Age = v_Age ;
	Salary = v_Salary ;
	Married = v_Married ;
	return true ;
    }

    public int GetAge(){ //mi 280
	return Age ;
    }
    
    public int GetSalary(){ //mi 287
	return Salary ;
    }

    public boolean GetMarried(){ // mi 288
	return Married ;
    }

    // This method returns true if the object "other"
    // has the same values for age, salary and 
    public boolean Equal(Element other){ //mi 285
	boolean ret_val ;
	int aux01 ;
	int aux02 ;
	int nt ;
	ret_val = true ;

	aux01 = other.GetAge();
	if (!this.Compare(aux01,Age)) ret_val = false ;
	else { 
	    aux02 = other.GetSalary();
	    if (!this.Compare(aux02,Salary)) ret_val = false ;
	    else 
		if (Married) 
		    if (!other.GetMarried()) ret_val = false;
		    else nt = 0 ;
		else
		    if (other.GetMarried()) ret_val = false;
		    else nt = 0 ;
	}

	return ret_val ;
    }

    // This method compares two integers and
    // returns true if they are equal and false
    // otherwise
    public boolean Compare(int num1 , int num2){ //mi 286
	boolean retval ;
	int aux02 ;
	retval = false ;
	aux02 = num2 + 1 ;
	if (num1 < num2) retval = false ;
	else if (!(num1 < aux02)) retval = false ;
	else retval = true ;
	return retval ;
    }

}

class List extends A{
    Element elem ;
    List next ;
    boolean end ;

    //List.<init> mi 273
    //
    // Initialize the node list as the last node
    public boolean Init(){ //mi 274
	end = true ;
	return true ;
    }

    // Initialize the values of a new node
    public boolean InitNew(Element v_elem, List v_next, boolean v_end){ //mi 279
	end = v_end ;
	elem = v_elem ;
	next = v_next ;
	return true ;
    }
    
    // Insert a new node at the beginning of the list
    public List Insert(Element new_elem){ //mi 278
	boolean ret_val ;
	List aux03 ;
	List aux02 ;
	aux03 = this ;
	aux02 = new List();
	ret_val = aux02.InitNew(new_elem,aux03,false);
	return aux02 ;
    }
    
    
    // Update the the pointer to the next node
    public boolean SetNext(List v_next){ //mi 290
	next = v_next ;
	return true ;
    }
    
    // Delete an element e from the list
    public List Delete(Element e){ //mi 289
	List my_head ;
	boolean ret_val ;
	boolean aux05;
	List aux01 ;
	List prev ;
	boolean var_end ;
	Element var_elem ;
	int aux04 ;
	int nt ;


	my_head = this ;
	ret_val = false ;
	aux04 = 0 - 1 ;
	aux01 = this ;
	prev = this ;
	var_end = end;
	var_elem = elem ;
	while ((!var_end) & (!ret_val)){
	    if (e.Equal(var_elem)){
		ret_val = true ;
		if (aux04 < 0) { 
		    // delete first element
		    my_head = aux01.GetNext() ;
		} 
		else{ // delete a non first element
		    System.out.println(0-555);
		    aux05 = prev.SetNext(aux01.GetNext());
		    System.out.println(0-555);
		    
		}
	    } else nt = 0 ;
	    if (!ret_val){
		prev = aux01 ;
		aux01 = aux01.GetNext() ;
		var_end = aux01.GetEnd();
		var_elem = aux01.GetElem();
		aux04 = 1 ; 
	    } else nt = 0 ;
	}
	return my_head ;
    }
    
    
    // Search for an element e on the list
    public int Search(Element e){ //mi 284
	int int_ret_val ;
	List aux01 ;
	Element var_elem ;
	boolean var_end ;
	int nt ;

	int_ret_val = 0 ;
	aux01 = this ;
	var_end = end;
	var_elem = elem ;
	while (!var_end){
	    if (e.Equal(var_elem)){
		int_ret_val = 1 ;
	    }
	    else nt = 0 ;
	    aux01 = aux01.GetNext() ;
	    var_end = aux01.GetEnd();
	    var_elem = aux01.GetElem();
	}
	return int_ret_val ;
    }
    
    public boolean GetEnd(){ //mi 282
	return end ;
    }
    
    public Element GetElem(){ //mi 283
	return elem ;
    }
    
    public List GetNext(){ //mi 281
	return next ;
    }
    
    
    // Print the linked list
    public boolean Print(){ //mi 275
	List aux01 ;
	boolean var_end ;
	Element  var_elem ;

	aux01 = this ;
	var_end = end ;
	var_elem = elem ;
	while (!var_end){
	    System.out.println(var_elem.GetAge());
	    aux01 = aux01.GetNext() ;
	    var_end = aux01.GetEnd();
	    var_elem = aux01.GetElem();
	}

	return true ;
    }
}
    

// this class invokes the methods to insert, delete,
// search and print the linked list
class LL{

	public LL () {} //mi 271

    public int Start(){ //mi 272 this-parm: 270-0

	List head ;
	List last_elem ;
	boolean aux01 ;
	Element el01 ;
	Element el02 ;
	Element el03 ;

	last_elem = new List(); //1->272-0
	aux01 = last_elem.Init();
	head = last_elem ; //head->272-0
	aux01 = head.Init();
	aux01 = head.Print();

	// inserting first element
	el01 = new Element(); //2->272-23
	aux01 = el01.Init(25,37000,false);
	head = head.Insert(el01);
	aux01 = head.Print();
	System.out.println(10000000);
	// inserting second  element
	el01 = new Element();
	aux01 = el01.Init(39,42000,true);
	el02 = el01 ;
	head = head.Insert(el01);
	aux01 = head.Print();
	System.out.println(10000000);
	// inserting third element
	el01 = new Element();
	aux01 = el01.Init(22,34000,false);
	head = head.Insert(el01);
	aux01 = head.Print();
	el03 = new Element();
	aux01 = el03.Init(27,34000,false);
	System.out.println(head.Search(el02));
	System.out.println(head.Search(el03));
	System.out.println(10000000);
	// inserting fourth element
	el01 = new Element();
	aux01 = el01.Init(28,35000,false);
	head = head.Insert(el01);
	aux01 = head.Print();
	System.out.println(2220000);

	head = head.Delete(el02);
	aux01 = head.Print();
	System.out.println(33300000);


	head = head.Delete(el01);
	aux01 = head.Print();
	System.out.println(44440000);
	
	return 0 ;
	
	
    }
    
}
