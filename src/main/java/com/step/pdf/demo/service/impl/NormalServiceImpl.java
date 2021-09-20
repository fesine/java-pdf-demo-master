package com.step.pdf.demo.service.impl;

import com.step.pdf.demo.service.NormalService;
import org.springframework.stereotype.Service;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/9/20
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/9/20
 */
@Service
public class NormalServiceImpl implements NormalService {



    @Override
    public void normal() {
        System.out.println("------>hello normal.");
    }
}
