package synthesis;

	public class MyOutput {
		StringBuffer S;

		public MyOutput(StringBuffer SB) {
			S = SB;
		}

		public void println(String s) {
			print(s);
			print("\r\n");
		}

		public void print(String s) {
			S.append(s);
		}


		public void append(StringBuffer s) {
					S.append(s);
		}
	}