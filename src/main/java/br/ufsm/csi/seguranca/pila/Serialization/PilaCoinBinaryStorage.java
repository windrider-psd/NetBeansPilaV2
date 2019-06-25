package br.ufsm.csi.seguranca.pila.Serialization;

import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinStorage;
import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class PilaCoinBinaryStorage implements PilaCoinStorage
{

    private String filePath;
    private File pilaCoinFile;

    private Set<PilaCoin> pilaCoinSet = new HashSet<>();

    private boolean autoSave;

    public PilaCoinBinaryStorage(String filePath, boolean autoSave) throws Exception
    {
        this.filePath = filePath;
        this.autoSave = autoSave;

        this.pilaCoinFile = new File(filePath);
        if (!this.pilaCoinFile.exists())
        {
            this.Save();
        }

        this.Load();
    }

    @Override
    public boolean Add(PilaCoin pilaCoin)
    {
        synchronized (this)
        {
            if (pilaCoinSet.contains(pilaCoin))
            {
                return false;
            }
            else
            {
                pilaCoinSet.add(pilaCoin);
                if (autoSave)
                {
                    return this.Save();
                }
                return true;
            }
        }
    }

    @Override
    public boolean Remove(PilaCoin pilaCoin)
    {
        synchronized (this)
        {
            if (pilaCoinSet.remove(pilaCoin))
            {
                if (autoSave)
                {
                    return this.Save();
                }
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @Override
    public boolean Update(PilaCoin pilaCoin)
    {
        PilaCoin p = this.Get(pilaCoin.getId());

        if (p != null)
        {
            this.pilaCoinSet.remove(p);
            this.pilaCoinSet.add(pilaCoin);

            if (this.autoSave)
            {
                this.Save();
            }
            return true;
        }
        else
        {
            return false;
        }

    }

    @Override
    public int Remove(PilaCoin[] pilaCoins)
    {
        int total = 0;
        for (PilaCoin p : pilaCoinSet)
        {
            if (this.Remove(p))
            {
                total++;
            }
        }
        return total;
    }

    @Override
    public PilaCoin Get(Long id)
    {
        PilaCoin pilaCoin = null;
        for (PilaCoin p : pilaCoinSet)
        {
            if (p.getId() == id)
            {
                pilaCoin = p;
                break;
            }
        }
        return pilaCoin;
    }

    @Override
    public boolean Contains(PilaCoin pilaCoin)
    {
        return false;
    }

    @Override
    public PilaCoin[] Get(Long[] ids)
    {
        Set<PilaCoin> foundSet = new HashSet<>();

        for (Long id : ids)
        {
            PilaCoin pilaCoin = this.Get(id);
            if (pilaCoin != null)
            {
                foundSet.add(pilaCoin);
            }
        }

        return foundSet.toArray(new PilaCoin[foundSet.size()]);
    }

    @Override
    public PilaCoin[] GetAll()
    {
        return this.pilaCoinSet.toArray(new PilaCoin[this.pilaCoinSet.size()]);
    }

    @Override
    public int Clear()
    {

        int total = this.pilaCoinSet.size();
        for (PilaCoin p : this.pilaCoinSet)
        {
        }
        this.pilaCoinSet.clear();

        this.Save();

        return total;
    }

    public boolean Save()
    {
        try
        {
            synchronized (this)
            {
                FileOutputStream fileOutputStream = new FileOutputStream(this.pilaCoinFile);
                SerializationUtils.SerializeObject(this.pilaCoinSet, fileOutputStream);
                fileOutputStream.close();
                return true;
            }
        }
        catch (IOException ex)
        {
            return false;
        }

    }

    private void Load() throws IOException, ClassNotFoundException
    {
        synchronized (this)
        {
            try
            {
                FileInputStream fileInputStream = new FileInputStream(this.pilaCoinFile);
                this.pilaCoinSet = (Set<PilaCoin>) SerializationUtils.DeserializeObject(fileInputStream);
                fileInputStream.close();
            }
            catch (EOFException ex)
            {
                FileOutputStream fileOutputStream = new FileOutputStream(this.pilaCoinFile);
                SerializationUtils.SerializeObject(this.pilaCoinSet, fileOutputStream);
                fileOutputStream.close();
            }

        }
    }
}
