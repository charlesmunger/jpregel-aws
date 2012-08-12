package api;

/**
 *
 * @author Pete Cappello
 */
abstract public class Aggregator<ElementType> implements java.io.Serializable
{
             protected ElementType element;
    
             public    Aggregator() { element = identityElement(); }
             
             public    Aggregator( ElementType element ) { this.element = element; }
      
    abstract public    void aggregate( Aggregator<ElementType> aggregator );
    
             public    ElementType get() { return element; }
    
    abstract public    ElementType identityElement();
    
             /*
              * @return a new Aggregator object
              */
   abstract  public   Aggregator make();
             
             public    void set( Aggregator<ElementType> aggregator ) { element = aggregator.get(); }            
}
