package com.atguigu.gmall.bean;

import javax.persistence.Id;
import java.io.Serializable;

public class PmsBaseCatalog3 implements Serializable {
    @Id

    private String id;
    private String name;
    private String catalog2_id;
}
