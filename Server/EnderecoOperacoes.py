# -*- coding: ISO-8859-1 -*-

from MemoryGame_pb2 import Endereco
import random

class EnderecoOperacoes:

    def __init__(self):
        self.ip = '224.1.2.3'
        self.portas = []    # lista com todas as portas jah usadas # random.randrange(10000, 65535)
        self.ids = []       # lista com todos os ids usados

        self.partidaEsperando = None

    def getIp(self):
        return self.ip

    def getPorta(self):
        return self.getValue(self.portas)

    def getId(self):
        return self.getValue(self.ids)

    # recupera um valor que nao esteja nesta lista
    # este valor deve ser um valor alto
    def getValue(self, lista):
        valor = random.randrange(10000, 36288)      # sorteia um valor

        while(valor in lista):                      # enquanto for sorteando um valor que jah esta sendo usado
            valor = random.randrange(10000, 36288)  # continua sorteando

        lista.append(valor)                         # guarda o valor na determinada lista
        return valor

    def criaEndereco(self):
        endereco = Endereco()
        endereco.id = self.getId()
        endereco.endereco = self.getIp()
        endereco.porta = self.getPorta()

        id = endereco.id
        # retorna o endereco jah em bytes
        return id, endereco.SerializeToString()

    def fimJogo(self, id):
        for i, idLista in enumerate(self.ids):
            if(idLista == id):      # achei a posicao do id
                # remove o id e a porta que aquele jogo estava
                self.ids.pop(i)
                self.portas.pop(i)