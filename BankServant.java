import org.omg.CORBA.ORB;
import java.util.HashMap;
import BankSystem.BankPOA;
import BankSystem.Transaction;
import BankSystem.Transfer;
import BankSystem.Interbank;
import BankSystem.IBankTransaction;
import BankSystem.NoSuchClientException;

import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;
import org.omg.CosNotifyComm.*;

public class BankServant
    extends BankPOA
    implements PushConsumerOperations
{
    private ORB m_ORB;
    private HashMap<Integer, Client> m_clientMap;
    private Object m_lastClientIdLock;
    private int m_lastClientId;
    private int m_bankID;
    private Interbank m_interBank;

    public void offer_change(EventType et[], EventType et2[]) // PushConsumer
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void disconnect_push_consumer() // PushConsumer
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void push(org.omg.CORBA.Any event_any) // PushConsumer
    {
        System.out.println("[Bank" + m_bankID + "]  received 1 event (push)");
        // Receive event here;
    }

    public BankServant(ORB orb, int bank_id, Interbank ibank)
    {
        m_ORB = orb;
        m_bankID = bank_id;
        m_lastClientIdLock = new Object();
        m_clientMap = new HashMap<Integer, Client>();
        ibank.register(bank_id, (IBankTransaction) this);
        m_interBank = ibank;
    }

    public void set_channel(EventChannel chan) // IBankTransaction
    {
        ProxyPushSupplier supp;
        supp = NotificationServiceHelper.getPPushSupplier(chan);

        try {
            supp.connect_any_push_consumer((PushConsumer) this);
        } catch (org.omg.CosEventChannelAdmin.AlreadyConnected ac) {
            // fine ...
        } catch (org.omg.CORBA.UserException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public int create_account() // IBankCustomer
    {
        int id;
        synchronized(m_lastClientIdLock) {
            id = m_lastClientId ++;
        }
        m_clientMap.put(id, new Client(0));
        return id;
    }

    public void close_account(int account_id)  // IBankCustomer
    {
        Client client;
        client = m_clientMap.get(account_id);
        if (client != null)
            m_clientMap.remove(client);
        client = null;
    }

    public void deposit(int account_id, int amount)  // IBankCustomer
    {
        Client client;
        client = m_clientMap.get(account_id);
        if (client != null)
            client.Deposit(amount);
    }

    public void withdraw(int account_id, int amount)  // IBankCustomer
    {
        Client client;
        client = m_clientMap.get(account_id);
        if (client != null)
            client.Withdraw(amount);
    }

    public int balance(int account_id) // IBankCustomer
      throws NoSuchClientException
    {
        Client client;
        client = m_clientMap.get(account_id);
        if (client != null)
            return client.GetBalance();
        else
            throw new NoSuchClientException();
    }

    public void do_transaction(Transaction t) // IBankTransaction
    /** this method WILL PROBABLY be removed later
     */
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
    }
}
