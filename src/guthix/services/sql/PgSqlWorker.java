package guthix.services.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;

/**
 * Created by Bart on 9/18/2015.
 *
 * Represents a worker for the queries and transactions.
 */
public class PgSqlWorker implements Runnable {

	private static final Logger logger = LogManager.getLogger(PgSqlWorker.class);

	private LinkedBlockingQueue<SqlTransaction> transactions = new LinkedBlockingQueue<>();
	private PgSqlService sqlService;
	private Connection connection;

	public PgSqlWorker(PgSqlService service) {
		sqlService = service;
	}

	public void submit(SqlTransaction transaction) {
		transactions.add(transaction);
	}

	@Override
	public void run() {
		while (true) {
			SqlTransaction transaction = null;

			try {
				transaction = transactions.take();
			} catch (Exception ignored) {
			}

			// Did we grab a transaction successfully?
			if (transaction == null)
				continue;

			// Have we got a valid connection?
			try {
				if (connection == null || connection.isClosed() || !connection.isValid(100)) {
					transactions.add(transaction);
					connection = sqlService.connection();
					Thread.sleep(250);
					continue;
				}
			} catch (Exception e) {
				transactions.add(transaction);
				e.printStackTrace();
				continue;
			}

			// Execute the query
			try {
				connection.setAutoCommit(false);
				transaction.execute(connection);
			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (SQLException ignored) {} // If that fails we might as well jump off a cliff

				// Yeah, we failed. Let's increase the counter.
				transaction.increaseAttempts();

				// We have a limit on 5 failures. If it's more, there's trouble going on. Retry, or notify.
				if (transaction.attempts() <= 5)
					transactions.add(transaction);
				else
					logger.error("Could not execute transaction {} after 5 attempts!", transaction, e);
			}
		}
	}

}
