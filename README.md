#CRONOGRAMA DE TAREFAS

	1. Use o registry para registrar e achar o mestre, com o nome “mestre”.

	2. Os escravos devem se registrar com o mestre ao serem inicializados, através de uma
	   operação oferecida pelo mestre (addSlave) e deve se “re-registrar” a cada 30 segundos.

		2.1. OBS: Atenção para a implementação de addSlave, pois como ela pode ser chamada
			por vários escravos simultaneamente, você deve proteger o seu código para acesso
			concorrente à lista de escravos.)

	3. Ao receber uma requisição do cliente, o mestre solicita a cada escravo registrado a
		realizar o ataque considerando uma parte do dicionário (passando índices das palavras
		iniciais e finais, como índice da primeira palavra igual a zero invocando a operação
		startSubAttack). Ao achar uma senha candidata, cada escravo informará esta senha
		ao mestre (através de callback, veja operação foundGuess).

		3.1. Os escravos devem informar o mestre a cada 10 segundos qual o índice da última palavra já testada como
			senha candidata (através de callback, veja operação checkpoint). Essa mesma
			operação deve ser usada também quando o escravo termina seu trabalho.

	4. Quando a invocação em um escravo gerar exceção (ou não ter enviado mensagem há
		mais de 20 segundos durante um ataque), este escravo deve ser removido da lista de
		escravos registrados. O trabalho deve ser redirecionado para outros escravos.

	5. O programa cliente deve: Receber um argumento na linha de comandos que indica o nome do arquivo que
		contém o vetor de bytes (com a mensagem criptografada) e outro argumento que
		indica a palavra conhecida que consta da mensagem. Caso o arquivo não exista, o
		cliente deve gerar o vetor de bytes aleatoriamente e salvá-lo em arquivo. Em um
		terceiro parâmetro pode ser especificado com o tamanho do vetor a ser gerado. Se
		esse terceiro parâmetro não existe, o tamanho do vetor deve ser gerado
		aleatoriamente (na faixa 1000 a 100000).

		5.1. Invocar o mestre passando o vetor de bytes, e imprimir chaves candidatas
			encontradas (se houver). Cada mensagem candidata deve ser colocada num
			arquivo com o nome da chave e a extensão .msg (por exemplo “house.msg” se a
			chave for house.)

	6. O mestre deve imprimir uma mensagem a cada callback recebido dos escravos; incluir o
		o nome do escravo nesta mensagem, índice atual (e mensagem candidata). Isto
		possibilitará verificar o andamento dos vários escravos.
		O mestre deve imprimir também os tempos medidos a partir de startSubAttack para
		cada checkpoint recebido, assim como o índice atual, para podermos verificar andamento
		no tempo e desempenho de cada escravo. Imprimir também quando receber o último
		checkpoint.
