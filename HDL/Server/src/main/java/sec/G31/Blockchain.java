package sec.G31;

import java.util.ArrayList;

public class Blockchain {
    private ArrayList<String> _blockChainList;
    
    public Blockchain(){
        _blockChainList = new ArrayList<String>();
    }
    
    public void addMessage(String msg){
        _blockChainList.add(msg);
    }

    public Boolean hasMessage(String msg){
        return _blockChainList.contains(msg);
    }

    public int getConsensusInstance(){
        return _blockChainList.size();
    }

    public String getLastDecidedValue(){
        return _blockChainList.get(_blockChainList.size());
    }
}
