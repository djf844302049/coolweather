package android.coolweather.com.coolweather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport{//省的数据信息
    private int id ;//每个实体类都该有的
    private String provinceName;//省的名字
    private int provinceCode;//省的代号
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getProvinceName(){
        return provinceName;
    }
    public void setProvinceName(String provinceName){
        this.provinceName = provinceName;
    }
    public int getProvinceCode(){
        return provinceCode;
    }
    public void setProvinceCode(int provinceCode){
        this.provinceCode = provinceCode;
    }
}