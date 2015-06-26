#//TODO

1. Mestre
	[X] Registrar no Registry
	[ ] Dividir o dicionário corretamente
	[ ] Solicitandar ataque por startSubAttack
	[ ] Redirecionar trabalho caso o escravo gere uma exceção
	[ ] Redirecionar trabalho caso o escravo não responda em 20 segundos
	[ ] Imprimir mensagem dos callbacks recebidos dos escravos
	[ ] Imprimir tempo e índice atual "a partir de startSubAttack" para cada checkpoint
	[ ] Imprimir quando receber o último checkpoint

2. Escravos
	[ ] Registrar no Mestre
	[ ] Re-registrar a cada 30 segundos
	[ ] addSlave não ocorrendo de forma concorrente
	[ ] Testar decrypt com senhas candidatas 
	[ ] Chamar foundGuess ao encontrar senha
	[ ] Enviar checkpoints a cada 10 segundos
	[ ] Enviar checkpoint final ao terminar a tarefa

3.Cliente
	[ ] Ler arquivo do argumento
	[ ] Gerar arquivo aleatório, caso não encontre arquivo do argumento
	[ ] Gerar arquivo aleatório com tamanho dado, caso não encontre arquivo do argumento
	[ ] Obter interface do Mestre
	[ ] Chamar função de ataque do mestre
	[ ] Imprimir chaves candidatas recebidas
	[ ] Colocar essas chaves em arquivos com nome <chave>.msg
	
4. Relatório
	[ ] Comandos para inicializar o Registry, o Cliente e os Escravos
	[ ] Descrever solução de Robustez
	[ ] Descrever testes
5. Análise

