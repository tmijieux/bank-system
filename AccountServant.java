import BankSystem.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CosNotification.*;
import org.omg.CosNotifyChannelAdmin.*;
import org.omg.CosNotifyComm.*;

public class AccountServant
    extends AccountPOA
{
    private int m_amount;
    private Interbank m_interbank;
    private int m_account_id;
    private int m_bank_id;

    public AccountServant(int amount, Interbank interbank,
                          int bank_id, int account_id)
    {
        m_amount = amount;
        m_bank_id = bank_id;
        m_account_id = account_id;
        m_interbank = interbank;
    }

    public int get_balance()
    {
        return m_amount;
    }

    public void deposit(int amount)
    {
        m_amount += amount;
    }

    public void withdraw(int amount)
    {
        m_amount -= amount;
    }

    public void transfer(int dst_bank_id, int dst_account_id, int amount)
    {
        Transfer F = new Transfer(m_bank_id, m_account_id,
                                  dst_bank_id, dst_account_id,
                                  amount);
        m_interbank.request_transfer(F);
    }

}
