package com.keoz5.zombiezcompanion.parser;

/**
 * Mutation state extracted from a bossbar.
 *
 * @param ready   true when the mutation is ready to trigger
 * @param name    display name of the mutation
 * @param percent bossbar fill percentage (0.0–1.0)
 */
public record ParsedMutationState(boolean ready, String name, float percent) {}
