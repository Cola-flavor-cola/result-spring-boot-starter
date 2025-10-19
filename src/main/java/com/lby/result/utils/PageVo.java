package com.lby.result.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 分页查询结果封装类，提供多种方式创建分页结果对象。
 *
 * <pre>
 *     {@code
 * // 通过MyBatis Plus的IPage转换
 * PageVo<User> pageVo1 = PageVo.of(userPage);
 *
 * // 手动指定分页参数
 * List<User> customRecords = fetchRecords();
 * PageVo<User> pageVo2 = PageVo.of(1, 10, 5, 50, customRecords);
 *
 * // 自定义记录数据但保留分页信息
 * PageVo<User> pageVo3 = PageVo.of(userPage, filteredRecords);
 * }
 * </pre>
 *
 * @param <T> 分页数据记录类型（如User、Product等业务实体类型）
 */
@Data
public class PageVo<T> {

    /**
     * 当前页码，表示用户正在查看的页数，从1开始。
     */
    private long current;

    /**
     * 每页数据量，表示每页显示的数据条数。
     */
    private long size;

    /**
     * 总记录数，表示查询结果中所有数据的总条数。
     */
    private long total;

    /**
     * 总页数，根据总记录数和每页数据量计算得出，表示分页的总页数。
     */
    private long pages;

    /**
     * 当前页数据集合，存储当前页的所有数据，如果无数据则为空列表。
     */
    private List<T> records;

    /**
     * 构造分页结果对象
     *
     * @param total   总记录数（必须非负）
     * @param pages   总页数（必须非负）
     * @param current 当前页码（必须非负）
     * @param size    每页大小（必须非负）
     * @param records 当前页数据列表（允许为null，自动转为空列表）
     * @throws IllegalArgumentException 如果数值参数为负数
     */
    private PageVo(long current, long size, long pages, long total, List<T> records) {
        // 参数有效性验证
        if (total < 0 || pages < 0 || current < 0 || size < 0) {
            throw new IllegalArgumentException("数值字段不能为负");
        }
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = pages;
        this.records = records != null ? records : Collections.emptyList();
    }

    /**
     * 将MyBatis Plus分页对象转换为PageVo分页结果对象
     * 
     * <pre>{@code
     * PageVo<User> pageVo = PageVo.of(userPage);
     * }</pre>
     *
     * @param <T>  分页数据记录类型
     * @param page MyBatis Plus分页对象
     * @return 包含完整分页信息的PageVo对象
     */
    public static <T> PageVo<T> of(IPage<T> page) {
        return new PageVo<>(
                page.getCurrent(),
                page.getSize(),
                page.getPages(),
                page.getTotal(),
                page.getRecords()
        );
    }

    /**
     * 通过MyBatis Plus分页对象和自定义记录数据创建并返回一个PageVo实例。
     * <p>
     * <pre>{@code
     * // 自定义记录数据并转换分页信息
     * List<User> customRecords = fetchCustomRecords();
     *
     * // 自动填充current、size、total、pages字段，并使用自定义records
     * PageVo<User> pageVo = PageVo.of(userPage, customRecords);
     *
     * }
     * </pre>
     *
     * @param <T>     分页数据记录类型
     * @param page    MyBatis Plus分页对象（不能为空，否则抛出NullPointerException）
     * @param records 当前页的实际记录数据列表（不能为空，否则抛出NullPointerException）
     * @return 包含完整分页信息和自定义记录数据的PageVo对象
     * @throws NullPointerException 若page或records参数为null
     */
    public static <T> PageVo<T> of(IPage<T> page, List<T> records) {
        // 确保分页对象不为 null，避免后续操作出现空指针异常。
        Objects.requireNonNull(page, "IPage 不能为null");
        // 确保记录数据不为 null，避免后续操作出现空指针异常。
        Objects.requireNonNull(records, "records 不能为null");

        // 使用分页对象和记录数据构造并返回一个新的 PageVo 实例。
        return new PageVo<>(
                page.getCurrent(),
                page.getSize(),
                page.getPages(),
                page.getTotal(),
                records
        );
    }

    /**
     * 通过手动指定分页参数创建并返回一个PageVo实例。
     * <p>
     * <pre> {@code
     * // 手动设置分页信息并创建PageVo对象
     * List<User> customRecords = fetchCustomRecords();
     *
     * // 自动填充current、size、total、pages字段，并使用自定义records
     * PageVo<User> pageVo = PageVo.of(1, 10, 5, 50, customRecords);
     * }
     * </pre>
     *
     * @param <T>     分页数据记录类型
     * @param current 当前页码（必须≥1，表示用户请求的页数）
     * @param size    每页数据量（必须≥1，表示每页显示的记录数）
     * @param pages   总页数（必须≥0，表示数据总共可以分成的页数）
     * @param total   总记录数（必须≥0，表示数据集中所有记录的总数）
     * @param records 当前页的实际记录数据列表（不能为空，否则抛出NullPointerException）
     * @return 包含完整分页信息和自定义记录数据的PageVo对象
     * @throws NullPointerException     若records参数为null
     * @throws IllegalArgumentException 若参数值不符合约束条件
     */
    public static <T> PageVo<T> of(long current, long size, long pages, long total, List<T> records) {
        // 确保分页对象不为 null，避免后续操作出现空指针异常。
        Objects.requireNonNull(records, "records 不能为null");
        // 参数有效性验证
        if (current < 1 || size < 1 || pages < 0 || total < 0) {
            throw new IllegalArgumentException("分页参数必须满足：current≥1, size≥1, pages≥0, total≥0");
        }

        // 使用分页对象和记录数据构造并返回一个新的 PageVo 实例。
        return new PageVo<>(
                current,
                size,
                pages,
                total,
                records
        );
    }

}