/**
 * a Bank from the point of view of Interbank
 *
 */

import org.omg.CORBA.ORB;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import BankSystem.InterbankPOA;
import BankSystem.Transfer;
import BankSystem.Transaction;
import BankSystem.IBankTransaction;

import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;
import org.omg.CosNotifyComm.*;

public class BankProxy
    implements PushSupplierOperations
{
    private int m_id;
    private IBankTransaction m_callback;
    private EventChannel m_chan;
    private ProxyPushConsumer m_ppconsumer;

    public void disconnect_push_supplier()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void subscription_change(EventType[] a, EventType[] b)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void sendTransaction(Transaction t)
    {
        org.omg.CORBA.Any ev = null;
        // build event:  ev.insert_data(...)

        try {
            m_ppconsumer.push(ev);
        } catch (org.omg.CosEventComm.Disconnected e) {
            e.printStackTrace();
            System.err.println("IMPOSSIBRRUUUUHH");
            // this object is supposed to in OUR process / on our machine
            // connexion problems MUST NOT happen here !!
            // if it does, there must a programming mistake or
            // a global comprehension problem
            System.exit(1);
        }
    }

    public BankProxy(int id, IBankTransaction callback, ORB orb)
    {
        m_id = id;
        m_callback = callback;
        m_chan = NotificationServiceHelper.createChannel(orb);
        m_ppconsumer = NotificationServiceHelper.getPPushConsumer(m_chan);

        try {
            m_ppconsumer.connect_any_push_supplier(
                (PushSupplier) /*this cast MAYBE is too ambitious*/ this);
        } catch (org.omg.CORBA.UserException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // client will instantiate a push_consumer implementation
        // and a proxy_push_supplier:
        m_callback.set_channel(m_chan);
    }
}
