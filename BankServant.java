import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import BankSystem.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Any;
import org.omg.PortableServer.*;
import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;
import org.omg.CosNotifyComm.*;

public class BankServant
    extends BankPOA
{
    private ORB m_ORB;
    private POA m_POA;
    private HashMap<Integer, Account> m_accountMap;
    private java.lang.Object m_lastAccountIdLock;
    private int m_lastAccountId;
    private int m_bank_id;
    private Interbank m_interbank;
    private BankMailBox m_mail_box;

    public BankServant(ORB orb, POA poa, int bank_id, Interbank interbank)
    {
        m_ORB = orb;
        m_POA = poa;
        m_bank_id = bank_id;
        m_lastAccountIdLock = new java.lang.Object();
        m_accountMap = new HashMap<Integer, Account>();

        m_interbank = interbank;
        m_interbank.register(m_bank_id, _this(orb));
    }

    public int get_id()
    {
        return m_bank_id;
    }

    public void set_channel(EventChannel chan) // IBankTransaction
    {
        m_mail_box = new BankMailBox(this, m_ORB, chan);
    }

    public int create_account() // IBankCustomer
    {
        int id;
        synchronized(m_lastAccountIdLock) {
            id = m_lastAccountId ++;
        }

        AccountServant ac_serv = new AccountServant(0, m_interbank, m_bank_id, id);
        try {
            m_POA.activate_object(ac_serv);
            Object ac_ref = m_POA.servant_to_reference(ac_serv);
            Account acc = AccountHelper.narrow(ac_ref);
            m_accountMap.put(id, acc);
        } catch (Exception e) {

        }
        return id;
    }

    public Account connect_account(int account_id)
    {
        return m_accountMap.get(account_id);
    }

    public void close_account(int account_id)  
    {
        Account account = m_accountMap.get(account_id);
        if (account != null)
            m_accountMap.remove(account);
        // deactivate
        account = null;
    }

    private boolean deposit(int account_id, int amount)
    {
        Account account;
        account = m_accountMap.get(account_id);
        if (account != null) {
            account.deposit(amount);
            return true;
        }
        return false;
    }

    private boolean withdraw(int account_id, int amount)
    {
        Account account;
        account = m_accountMap.get(account_id);

        if (account != null) {
            account.withdraw(amount);
            return true;
        }
        return false;
    }

    public void handleTransaction(Transaction T)
    {
        if (T.bank_id != m_bank_id)
            return;

        if (T.type == TransactionType.CREDIT)
            this.withdraw(T.account_id, T.amount);
        else if (T.type == TransactionType.DEBIT) {
            boolean success = this.deposit(T.account_id, T.amount);
            m_interbank.confirm_transaction(T.id, success);
        }
    }
}
