import org.omg.CORBA.ORB;
import java.util.HashMap;
import BankSystem.BankPOA;
import BankSystem.Transaction;
import BankSystem.Transfer;
import BankSystem.Interbank;
import BankSystem.IBankTransaction;
import BankSystem.NoSuchClientException;

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
      if (client != null)
          m_clientMap.remove(client);
      client = null;
  }

  public void deposit(int account_id, int amount)
  {
      Client client;
      client = m_clientMap.get(account_id);
      if (client != null)
          client.Deposit(amount);
  }

  public void withdraw(int account_id, int amount)
  {
      Client client;
      client = m_clientMap.get(account_id);
      if (client != null)
          client.Withdraw(amount);
  }

  public int balance(int account_id)
      throws NoSuchClientException
  {
      Client client;
      client = m_clientMap.get(account_id);
      if (client != null)
          return client.GetBalance();
      else
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
  }
}
