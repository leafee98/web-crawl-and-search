package data;

import lombok.Data;

import java.util.List;

@Data
public class Phone {
    private String title;
    private String skuId;
    private String url;
    private List<String> info;
}
