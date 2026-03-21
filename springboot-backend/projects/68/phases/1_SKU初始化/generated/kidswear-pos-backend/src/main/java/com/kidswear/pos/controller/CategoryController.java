package com.kidswear.pos.controller;

import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Category;
import com.kidswear.pos.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list/enabled")
    public Result<List<Category>> getEnabledList() {
        return categoryService.getEnabledList();
    }

    @PostMapping
    public Result<Void> add(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }
}