import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;
import org.omg.CosNotifyComm.*;

public final class NotificationServiceHelper
{
    private NotificationServiceHelper() {}

    public static EventChannel createChannel(org.omg.CORBA.ORB orb)
    {
        org.omg.CORBA.Object chan_factory_ref = null;
        EventChannelFactory ev_chan_factory;
        try {
            chan_factory_ref =
                orb.resolve_initial_references("NotificationService");
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            e.printStackTrace();
            System.exit(1);
        }

        ev_chan_factory = EventChannelFactoryHelper.narrow(chan_factory_ref);
        org.omg.CORBA.IntHolder id = new org.omg.CORBA.IntHolder();
        Property[] props = new Property[0];
        EventChannel chan= null;
        try {
            chan = ev_chan_factory.create_channel(props, props, id);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return chan;
    }

    public static ProxyPushConsumer getPPushConsumer(EventChannel chan)
    {
        SupplierAdmin supp_adm;
        supp_adm = chan.default_supplier_admin();

        org.omg.CORBA.IntHolder supp_id = new org.omg.CORBA.IntHolder();
        ProxyConsumer generic_pconsumer = null;
        try {
            generic_pconsumer = supp_adm.obtain_notification_push_consumer(
                ClientType.ANY_EVENT, supp_id);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return ProxyPushConsumerHelper.narrow(generic_pconsumer);
    }

    public static ProxyPushSupplier getPPushSupplier(EventChannel chan)
    {
        ConsumerAdmin consu_adm;
        consu_adm = chan.default_consumer_admin();

        org.omg.CORBA.IntHolder consu_id = new org.omg.CORBA.IntHolder();
        ProxySupplier generic_psupplier = null;
        try {
            generic_psupplier = consu_adm.obtain_notification_push_supplier(
                ClientType.ANY_EVENT, consu_id);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return ProxyPushSupplierHelper.narrow(generic_psupplier);
    }
}
