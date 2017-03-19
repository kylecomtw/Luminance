/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 *
 * @author Sean_S325
 */
public class FieldTypeFactory {
    public enum FTEnum {FullIndex, SimpleIndex, RawIndex, RawStoredIndex}
    public static FieldType Get(FTEnum ft){
        switch (ft){
            case FullIndex:
                return getFullIndexedFieldType();                
            case SimpleIndex:
                return getSimpleIndexFieldType();                
            case RawIndex:
                return getIndexedRawFieldType();
            case RawStoredIndex:
                return getIndexedRawStoredFieldType();
            default:
                return getSimpleIndexFieldType();
        }
    }
    
    private static FieldType getIndexedRawFieldType(){
        FieldType raw_ft = new FieldType();
        raw_ft.setTokenized(false);
        raw_ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        return raw_ft;
    }
    
    private static FieldType getIndexedRawStoredFieldType(){
        FieldType raw_ft = new FieldType();
        raw_ft.setTokenized(false);
        raw_ft.setStored(true);
        raw_ft.setIndexOptions(IndexOptions.DOCS);
        return raw_ft;
    }
    
    private static FieldType getSimpleIndexFieldType(){
        FieldType indexed_ft = new FieldType();
        indexed_ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        
        return indexed_ft;
    }
    
    private static FieldType getFullIndexedFieldType(){
        FieldType ftype = new FieldType();        
        ftype.setStored(true);
        ftype.setTokenized(true);
        ftype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        ftype.setStoreTermVectors(true);
        ftype.setStoreTermVectorOffsets(true);
        ftype.setStoreTermVectorPositions(true);
        ftype.setStoreTermVectorPayloads(true);
        
        return ftype;
    }
}
