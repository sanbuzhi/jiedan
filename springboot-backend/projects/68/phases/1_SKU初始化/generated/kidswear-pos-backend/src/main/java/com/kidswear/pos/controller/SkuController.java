package com.kidswear.pos.controller;

import com.kidswear.pos.common.PageResult;
import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Sku;
import com.kidswear.pos.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sku")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    @GetMapping("/page")
    public Result<PageResult<Sku>> getPage(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String spuName,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer status) {
        return skuService.getPage(current, size, spuName, categoryId, status);
    }

    @GetMapping("/code")
    public Result<String> generateSkuCode() {
        return skuService.generateSkuCode();
    }

    @GetMapping("/{id}")
    public Result<Sku> getById(@PathVariable Long id) {
        return Result.success(skuService.getById(id));
    }

    @GetMapping("/code/{skuCode}")
    public Result<Sku> getBySkuCode(@PathVariable String skuCode) {
        return skuService.getBySkuCode(skuCode);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Sku sku) {
        skuService.save(sku);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Sku sku) {
        skuService.updateById(sku);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skuService.removeById(id);
        return Result.success();
    }
}