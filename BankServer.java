import java.util.Properties;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.*;
import org.omg.PortableServer.Servant;

public class BankServer {

    public static void main(String args[]) {
        Properties properties = System.getProperties();
        properties.put( "org.omg.CORBA.ORBInitialHost", "localhost" );
        properties.put( "org.omg.CORBA.ORBInitialPort", "1050" );

        int id = Integer.parseInt(
            properties.getProperty(
                "fr.enseirb-matmeca.vvv.tmijieux.projetCORBA.BankID"));

        try {
            ORB orb = ORB.init(args, properties);
            BankServant servant = new BankServant(orb, id, /*FIXME*/null);
            POA rootPOA = POAHelper.narrow(
                orb.resolve_initial_references("RootPOA"));
            Policy[] persistentPolicy = new Policy[1];
            persistentPolicy[0] = rootPOA.create_lifespan_policy(
                LifespanPolicyValue.PERSISTENT);
            POA persistentPOA = rootPOA.create_POA("childPOA", null,
                                                   persistentPolicy );
            persistentPOA.the_POAManager().activate( );
            persistentPOA.activate_object( servant );
            org.omg.CORBA.Object obj;
            obj = orb.resolve_initial_references("NameService" );
            NamingContextExt rootContext = NamingContextExtHelper.narrow(obj);

            NameComponent[] nc;
            nc = rootContext.to_name("BankServer"+id);
            rootContext.rebind(nc, persistentPOA.servant_to_reference(servant));
            orb.run();
        } catch ( Exception e ) {
            System.err.println("Exception in Interbank Server Startup " + e);
        }
    }
}
