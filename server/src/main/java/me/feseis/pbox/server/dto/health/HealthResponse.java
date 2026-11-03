package me.feseis.pbox.server.dto.health;

/**
 * 健康检查出参。数据库可用才返回 200，不可用由异常出口返回 503。
 */
public record HealthResponse(boolean database) {
}
