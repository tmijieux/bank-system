public class Client
{
    private int m_amount;

    public Client(int amount)
    {
        m_amount = amount;
    }

    public int GetBalance()
    {
        return m_amount;
    }

    public void Deposit(int amount)
    {
        m_amount += amount;
    }

    public void Withdraw(int amount)
    {
        m_amount -= amount;
    }
}
