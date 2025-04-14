package ink.usr.common.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysCategoryModel implements Serializable {
    // 设备ID
    private long categoryId;
    // 设备大类
    private String categoryMain;
    // 设备小类
    private String categoryType;
    // 设备品牌
    private String categoryBrand;
    // 设备名
    private String categoryDname;
    // 设备价格
    private long categoryPrice;
    // 设备供应商
    private String categorySupplier;
    // 设备使用年限
    private long categoryUseyear;
    // 设备库存数
    private long categorySafenum;
    // 设备易损性
    private String categoryProperty;
    // 设备标准性
    private String categoryStype;
    // 设备描述
    private String categoryDescription;
}
