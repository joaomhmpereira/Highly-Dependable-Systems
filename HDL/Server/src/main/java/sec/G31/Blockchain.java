package sec.G31;

import java.util.ArrayList;

import sec.G31.utils.TransactionBlock;

public class Blockchain {
    private ArrayList<TransactionBlock> _blockChainList;
    
    public Blockchain(){
        _blockChainList = new ArrayList<TransactionBlock>();
    }
    
    public void addTransactionBlock(TransactionBlock block){
        _blockChainList.add(block);
    }

    public Boolean hasBlock(TransactionBlock block){
        return _blockChainList.contains(block);
    }

    public int getConsensusInstance(){
        return _blockChainList.size();
    }

    public TransactionBlock getLastDecidedValue(){
        if (_blockChainList.size() == 0){
            return null;
        }
        return _blockChainList.get(_blockChainList.size()-1);
    }

    @Override
    public String toString(){
        String result = "";
        for(TransactionBlock block : _blockChainList){
            result += block.toString();
        }
        return result;
    }
}
