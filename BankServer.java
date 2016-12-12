import java.util.Properties;

import BankSystem.*;

import org.omg.CORBA.Object;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.*;

public class BankServer
{
    private static POA makePersistantPOA(ORB orb)
      throws org.omg.CORBA.UserException
    {
        POA rootPOA = POAHelper.narrow(
            orb.resolve_initial_references("RootPOA"));
        Policy[] persistentPolicy = new Policy[1];
        persistentPolicy[0] = rootPOA.create_lifespan_policy(
            LifespanPolicyValue.PERSISTENT);
        POA persistentPOA = rootPOA.create_POA(
            "PersistentPOA", null, persistentPolicy );

        persistentPOA.the_POAManager().activate();
        return persistentPOA;
    }

    public static void main(String args[])
    {
        Properties properties = System.getProperties();
        properties.put( "org.omg.CORBA.ORBInitialHost", "localhost" );
        properties.put( "org.omg.CORBA.ORBInitialPort", "1050" );

        int id;
        try {
            id = Integer.parseInt(
                properties.getProperty(
                    "fr.enseirb-matmeca.vvv.tmijieux.projetCORBA.BankID"));
        } catch (Exception e) {
            id = 0;
        }
        
        try {
            ORB orb = ORB.init(args, properties);
            POA persistentPOA = makePersistantPOA(orb);

            Object obj = orb.resolve_initial_references("NameService");
            NamingContextExt rootContext = NamingContextExtHelper.narrow(obj);

            obj = rootContext.resolve_str("InterbankServer");
            Interbank interbank = InterbankHelper.narrow(obj);
            BankServant servant = new BankServant(orb, persistentPOA,
                                                  id, interbank);
            persistentPOA.activate_object( servant );

            NameComponent[] nc = rootContext.to_name("Bank"+id+"Server");
            rootContext.rebind(nc, persistentPOA.servant_to_reference(servant));
            orb.run();

        } catch ( Exception e ) {
            e.printStackTrace();
            System.err.println("Exception in Interbank Server Startup " + e);
        }
    }
}
