package com.vermouthx.stocker

import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

/**
 * 股票应用管理器（单例）
 * 
 * 负责管理所有项目窗口的StockerApp实例。
 * IntelliJ IDEA支持同时打开多个项目，每个项目都有独立的StockerApp实例。
 * 
 * 核心职责：
 * 1. 维护项目到StockerApp的映射关系
 * 2. 提供线程安全的实例获取和创建
 * 3. 管理实例的生命周期
 * 4. 确保资源正确释放
 * 
 * 线程安全：
 * - 使用ConcurrentHashMap避免并发问题
 * - 提供原子性的getOrCreate操作
 * - 支持安全的remove操作
 * 
 * 使用场景：
 * - 工具窗口创建时获取StockerApp实例
 * - 项目关闭时清理StockerApp实例
 * - Action类中访问应用实例
 * 
 * @author VermouthX
 */
object StockerAppManager {
    
    /**
     * 项目到应用实例的映射
     * 
     * 使用ConcurrentHashMap保证线程安全：
     * - 支持并发读写
     * - 无需额外同步
     * - 提供原子性操作
     * 
     * key: Project - IntelliJ的项目实例
     * value: StockerApp - 对应的股票应用实例
     */
    val myApplicationMap: MutableMap<Project, StockerApp> = ConcurrentHashMap()

    /**
     * 获取指定项目的StockerApp实例
     * 
     * 简单的获取方法，不会自动创建新实例。
     * 如果项目不存在对应的实例，返回null。
     * 
     * @param project 项目实例（可为null）
     * @return 对应的StockerApp实例，不存在时返回null
     */
    fun myApplication(project: Project?): StockerApp? {
        return project?.let { myApplicationMap[it] }
    }
    
    /**
     * 获取或创建StockerApp实例（线程安全）
     * 
     * 使用ConcurrentHashMap的computeIfAbsent方法，保证原子性：
     * - 如果实例已存在，直接返回
     * - 如果实例不存在，创建新实例并保存
     * - 整个操作是原子性的，避免并发创建多个实例
     * 
     * 适用场景：
     * - 工具窗口首次打开时
     * - 确保每个项目有且仅有一个StockerApp实例
     * 
     * @param project 项目实例
     * @return 对应的StockerApp实例（新建或已存在）
     */
    fun getOrCreate(project: Project): StockerApp {
        return myApplicationMap.computeIfAbsent(project) { StockerApp() }
    }
    
    /**
     * 移除并关闭指定项目的StockerApp
     * 
     * 用于项目关闭时清理资源：
     * 1. 从映射中移除实例
     * 2. 关闭实例的线程池
     * 3. 停止定时任务
     * 
     * 资源管理：
     * - 确保线程池正确关闭
     * - 防止内存泄漏
     * - 释放网络连接
     * 
     * @param project 要关闭的项目实例
     */
    fun removeAndShutdown(project: Project) {
        myApplicationMap.remove(project)?.shutdown()
    }
}