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
    // 种类ID
    private long categoryId;
    // 设备大类
    private long categoryMain;
    // 设备小类
    private long categoryType;
    // 设备品牌
    private long categoryBrand;
    // 设备名
    private long categoryDname;
    // 设备价格
    private long categoryPrice;
    // 设备供应商
    private long categorySupplier;
    // 设备使用年限
    private long categoryUseyear;
    // 设备库存数
    private long categorySafenum;
    // 设备易损性
    private long categoryProperty;
    // 设备标准性
    private long categoryStype;
    // 设备描述
    private long categoryDescription;
}
