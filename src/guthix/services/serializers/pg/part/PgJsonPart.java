package guthix.services.serializers.pg.part;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import guthix.model.entity.Player;

/**
 * Created by Bart on 8/10/2015.
 */
public interface PgJsonPart {

	public void decode(Player player, ResultSet resultSet) throws SQLException;

	public void encode(Player player, PreparedStatement characterUpdateStatement) throws SQLException;

}
