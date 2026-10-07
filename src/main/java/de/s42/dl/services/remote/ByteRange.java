package de.s42.dl.services.remote;

public final class ByteRange
{

	public static final ByteRange UNSATISFIABLE = new ByteRange(-1, -1);

	private static final String UNIT = "bytes=";

	public final long start;
	public final long end;

	private ByteRange(long start, long end)
	{
		this.start = start;
		this.end = end;
	}

	public long getLength()
	{
		return end - start + 1;
	}

	public boolean isSatisfiable()
	{
		return this != UNSATISFIABLE;
	}

	/**
	 * Parses a single-range HTTP Range header against a representation of the given total length.
	 *
	 * @return null if the header is absent or has to be ignored (malformed, multiple ranges, other unit),
	 * UNSATISFIABLE if the range lies outside of the representation, otherwise the resolved range
	 */
	public static ByteRange parse(String header, long total)
	{
		if (header == null || total < 0) {
			return null;
		}

		String value = header.trim();

		if (value.length() < UNIT.length() || !value.regionMatches(true, 0, UNIT, 0, UNIT.length())) {
			return null;
		}

		value = value.substring(UNIT.length()).trim();

		if (value.contains(",")) {
			return null;
		}

		int dash = value.indexOf('-');

		if (dash < 0) {
			return null;
		}

		String first = value.substring(0, dash).trim();
		String last = value.substring(dash + 1).trim();

		try {
			if (first.isEmpty()) {

				if (last.isEmpty()) {
					return null;
				}

				long suffix = Long.parseLong(last);

				if (suffix < 0) {
					return null;
				}

				if (suffix == 0 || total == 0) {
					return UNSATISFIABLE;
				}

				return new ByteRange(Math.max(0, total - suffix), total - 1);
			}

			long start = Long.parseLong(first);

			if (start < 0) {
				return null;
			}

			long end = last.isEmpty() ? Long.MAX_VALUE : Long.parseLong(last);

			if (end < start) {
				return null;
			}

			if (start >= total) {
				return UNSATISFIABLE;
			}

			return new ByteRange(start, Math.min(end, total - 1));
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}