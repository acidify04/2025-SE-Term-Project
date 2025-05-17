package main.java.com.yutgame.dto;

import main.java.com.yutgame.model.Piece;
import main.java.com.yutgame.model.Player;
import main.java.com.yutgame.model.YutThrowResult;

import java.util.List;

public record PieceDecisionResult(List<String> decisions, List<Piece> choices) {}

