import { useState } from "react";
import type { Error } from "./types.ts";
import { errorMockData } from "./data/mock.ts";

const App = () => {
	const [error] = useState<Error>(errorMockData);
	console.log(error);

	return (
		<>Hello World!</>
	);
};

export default App;
