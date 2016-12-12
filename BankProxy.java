/**
 * a Bank from the point of view of Interbank
 *
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import BankSystem.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CosNotification.*;
import org.omg.CosNotifyComm.*;
import org.omg.CosNotifyChannelAdmin.*;

public class BankProxy
{
    private ORB m_ORB;
    private int m_id;
    private EventChannel m_chan;
    private ProxyPushConsumer m_proxy_consumer;

    private Any createAnyTransaction(Transaction T)
    {
        String event_name = "transaction"+T.id;
        EventType ev_type = new EventType("INTERBANK", "transaction");

        FixedEventHeader fixed_h =
            new FixedEventHeader(ev_type, event_name.toString());
        Property[] var_h = new Property[0];
        EventHeader header = new EventHeader(fixed_h, var_h);

        Property[] filterable_data = new Property[3];
        filterable_data[0] = new Property("filter:account_id", m_ORB.create_any());
        filterable_data[1] = new Property("filter:debit", m_ORB.create_any());
        filterable_data[2] = new Property("filter:amount", m_ORB.create_any());

        filterable_data[0].value.insert_string("" + T.account_id);
        filterable_data[1].value.insert_string("" + T.type);
        filterable_data[2].value.insert_string("" + T.amount);

        Any msg = m_ORB.create_any();
        msg.insert_string("");

        StructuredEvent st_ev = new StructuredEvent(header, filterable_data, msg);

        Any ev = m_ORB.create_any();
        StructuredEventHelper.insert(ev, st_ev);
        return ev;
    }

    public void sendTransaction(Transaction t)
    {
        Any ev = createAnyTransaction(t);

        try {
            m_proxy_consumer.push(ev);
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

    public BankProxy(int id, Bank_IInterbank bank_callback, ORB orb)
    {
        m_id = id;
        m_chan = NotificationServiceHelper.createChannel(orb);
        m_proxy_consumer = NotificationServiceHelper.getPPushConsumer(m_chan);

        try {
            m_proxy_consumer.connect_any_push_supplier( null );
        } catch (org.omg.CORBA.UserException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // client will instantiate a push_consumer implementation
        // and a proxy_push_supplier:
        bank_callback.set_channel(m_chan);
    }
}
