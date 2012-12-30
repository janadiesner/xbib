package jacc.grammar;

import java.io.PrintWriter;

public final class Finitary extends Analysis
{

    private boolean finitary[];
    private boolean consider[];
    private Grammar grammar;
    private int numNTs;

    public Finitary(Grammar grammar1)
    {
        super(grammar1.getComponents());
        grammar = grammar1;
        numNTs = grammar1.getNumNTs();
        finitary = new boolean[numNTs];
        consider = new boolean[numNTs];
        for (int i = 0; i < numNTs; i++)
        {
            finitary[i] = false;
            consider[i] = true;
        }

        bottomUp();
    }

    protected boolean analyze(int i)
    {
        boolean flag = false;
        if (consider[i])
        {
            int j = 0;
            Grammar.Prod aprod[] = grammar.getProds(i);
            for (int k = 0; k < aprod.length; k++)
            {
                int ai[] = aprod[k].getRhs();
                int l;
                for (l = 0; l < ai.length && at(ai[l]); l++);
                if (l >= ai.length)
                {
                    finitary[i] = true;
                    consider[i] = false;
                    flag = true;
                    break;
                }
                if (!consider[ai[l]])
                    j++;
            }

            if (j == aprod.length)
                consider[i] = false;
        }
        return flag;
    }

    public boolean at(int i)
    {
        return grammar.isTerminal(i) || finitary[i];
    }

    public void display(PrintWriter printwriter)
    {
        printwriter.print("Finitary = {");
        int i = 0;
        for (int j = 0; j < numNTs; j++)
        {
            if (!at(j))
                continue;
            if (i > 0)
                printwriter.print(", ");
            printwriter.print(grammar.getSymbol(j).getName());
            i++;
        }

        printwriter.println("}");
    }
}
