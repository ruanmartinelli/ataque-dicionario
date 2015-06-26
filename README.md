#TODO

#####1. Mestre
	[X] Registrar no Registry
	[X] Dividir o dicionário corretamente
	[X] Solicitandar ataque por startSubAttack
	[ ] Redirecionar trabalho caso o escravo gere uma exceção
	[ ] Redirecionar trabalho caso o escravo não responda em 20 segundos
	[ ] Imprimir mensagem dos callbacks recebidos dos escravos
	[ ] Imprimir tempo(que tempo?) e índice atual "a partir de startSubAttack" para cada checkpoint
	[X] Imprimir quando receber o último checkpoint
	[ ] Acertar ID dos escravos para ler na HashMap do Mestre

#####2. Escravos
	[X] Registrar no Mestre
	[X] Re-registrar a cada 30 segundos
	[ ] addSlave não ocorrendo de forma concorrente
	[ ] Testar decrypt com senhas candidatas 
	[X] Chamar foundGuess ao encontrar senha
	[X] Enviar checkpoints a cada 10 segundos
	[X] Enviar checkpoint final ao terminar a tarefa

#####3.Cliente
	[ ] Ler arquivo do argumento
	[X] Gerar arquivo aleatório, caso não encontre arquivo do argumento
	[X] Gerar arquivo aleatório com tamanho dado, caso não encontre arquivo do argumento
	[X] Obter interface do Mestre
	[X] Chamar função de ataque do mestre
	[X] Imprimir chaves candidatas recebidas
	[ ] Colocar essas chaves em arquivos com nome <chave>.msg
	
#####4. Relatório
	[ ] Comandos para inicializar o Registry, o Cliente e os Escravos
	[ ] Descrever solução de Robustez
	[ ] Descrever testes
#####5. Análise

