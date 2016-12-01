import org.omg.CORBA.ORB;

import java.util.HashMap;
import BankSystem.BankPOA;
import BankSystem.Transaction;
import BankSystem.Transfer;
import BankSystem.Interbank;
import BankSystem.IBankTransaction;
import BankSystem.NoSuchClientException;

//import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;

public class BankServant
    extends BankPOA
{
    private ORB m_ORB;
    private HashMap<Integer, Client> m_clientMap;
    private Object m_lastClientIdLock;
    private int m_lastClientId;
    private int m_bankID;
    private Interbank m_interBank;

    public BankServant(ORB orb, int bank_id, Interbank ibank)
    {
        m_ORB = orb;
        m_bankID = bank_id;
        m_lastClientIdLock = new Object();
        m_clientMap = new HashMap<Integer, Client>();
        ibank.register(bank_id, (IBankTransaction) this);
        m_interBank = ibank;
    }

    public int create_account()
    {
        int id;
        synchronized(m_lastClientIdLock) {
            id = m_lastClientId ++;
        }
        m_clientMap.put(id, new Client(0));
        return id;
    }

    public void close_account(int account_id)
    {
        Client client;
        client = m_clientMap.get(account_id);
        if (client != null) {
            m_clientMap.remove(client);
        }
        client = null;
    }

    public void deposit(int account_id, int amount)
    {
        Client client;
        client = m_clientMap.get(account_id);
        if (client != null) {
            client.Deposit(amount);
        }
    }

    public void withdraw(int account_id, int amount)
    {
        Client client;
        client = m_clientMap.get(account_id);
        if (client != null) {
            client.Withdraw(amount);
        }
    }

    public int balance(int account_id)
      throws NoSuchClientException
    {
        Client client;
        client = m_clientMap.get(account_id);
        if (client != null) {
            return client.GetBalance();
        } else
            throw new NoSuchClientException();
    }

    public void do_transaction(Transaction t)
    {
        Client client;
        client = m_clientMap.get(t.accountID);
        if (client != null) {
            if (t.isDebit)
                client.Deposit(t.amount);
            else
                client.Withdraw(t.amount);
        }
    }

    public void transfer(Transfer t)
    {
        // create the event channel
        org.omg.CORBA.Object chan_factory_ref;
        EventChannelFactory ev_chan_factory;
        EventChannel ev_chan;

        chan_factory_ref = m_ORB.resolve_initial_references("NotificationService");
        ev_chan_factory = EventChannelFactoryHelper.narrow(chan_factory_ref);
        ev_chan = ev_chan_factory.create_eventchannel();

        // creating and posting events
        SupplierAdmin supp_adm;
        ProxyConsumer generic_proxy_consumer;
        StructuredProxyPushConsumer proxy_push_consumer;

        supp_adm = ev_chan.default_supplier_admin();
        generic_proxy_consumer = supp_adm.obtain_notification_push_consumer();

        proxy_push_consumer =
            StructuredProxyPushConsumerHelper.narrow(generic_proxy_consumer);
        proxy_push_consumer.connect_structured_push_supplier();

        // ConsumerAdmin cons_adm;
        // StructuredPushSupplier push_supp;
    }
}
